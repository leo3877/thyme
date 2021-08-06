package org.yixi.thyme.core;

import com.fasterxml.jackson.annotation.JsonValue;
import java.io.Serializable;
import java.net.NetworkInterface;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>A globally unique identifier for objects.</p>
 * <p>Consists of 12 bytes, divided as follows:</p> <table border="1"> <caption>ObjectID
 * layout</caption> <tr> <td>0</td><td>1</td><td>2</td><td>3</td><td>4</td><td>5</td><td>6</td
 * ><td>7</td><td>8</td><td>9</td><td>10</td><td>11</td>
 * </tr> <tr> <td colspan="4">time</td><td colspan="3">machine</td> <td colspan="2">pid</td><td
 * colspan="3">inc</td> </tr> </table>
 * <p>Instances of this class are immutable.</p>
 *
 * @author mongo
 * @mongodb.driver.manual com.wacai.creditcard.core/object-id ObjectId
 */
public final class ObjectId implements Comparable<ObjectId>, Serializable {

  private static final long serialVersionUID = 3670079982654483072L;

  private static final int LOW_ORDER_THREE_BYTES = 0x00ffffff;

  private static final int MACHINE_IDENTIFIER;
  private static final short PROCESS_IDENTIFIER;
  private static final AtomicInteger NEXT_COUNTER = new AtomicInteger(
    new SecureRandom().nextInt());

  private static final char[] HEX_CHARS = new char[]{
    '0', '1', '2', '3', '4', '5', '6', '7',
    '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

  private final int timestamp;
  private final int machineIdentifier;
  private final short processIdentifier;
  private final int counter;

  public static ObjectId get() {
    return new ObjectId();
  }

  public static boolean isValid(final String hexString) {
    if (hexString == null) {
      throw new IllegalArgumentException();
    }

    int len = hexString.length();
    if (len != 24) {
      return false;
    }

    for (int i = 0; i < len; i++) {
      char c = hexString.charAt(i);
      if (c >= '0' && c <= '9') {
        continue;
      }
      if (c >= 'a' && c <= 'f') {
        continue;
      }
      if (c >= 'A' && c <= 'F') {
        continue;
      }

      return false;
    }

    return true;
  }

  public static int getGeneratedMachineIdentifier() {
    return MACHINE_IDENTIFIER;
  }

  public static int getGeneratedProcessIdentifier() {
    return PROCESS_IDENTIFIER;
  }

  public static int getCurrentCounter() {
    return NEXT_COUNTER.get();
  }

  public static ObjectId createFromLegacyFormat(final int time, final int machine,
    final int inc) {
    return new ObjectId(time, machine, inc);
  }

  public ObjectId() {
    this(new Date());
  }

  public ObjectId(final Date date) {
    this(dateToTimestampSeconds(date), MACHINE_IDENTIFIER, PROCESS_IDENTIFIER,
      NEXT_COUNTER.getAndIncrement(), false);
  }

  public ObjectId(final Date date, final int counter) {
    this(date, MACHINE_IDENTIFIER, PROCESS_IDENTIFIER, counter);
  }

  public ObjectId(final Date date, final int machineIdentifier, final short processIdentifier,
    final int counter) {
    this(dateToTimestampSeconds(date), machineIdentifier, processIdentifier, counter);
  }

  public ObjectId(final int timestamp, final int machineIdentifier, final short processIdentifier,
    final int counter) {
    this(timestamp, machineIdentifier, processIdentifier, counter, true);
  }

  private ObjectId(final int timestamp, final int machineIdentifier,
    final short processIdentifier, final int counter,
    final boolean checkCounter) {
    if ((machineIdentifier & 0xff000000) != 0) {
      throw new IllegalArgumentException(
        "The machine identifier must be between 0 and 16777215 (it must fit in three "
          + "bytes).");
    }
    if (checkCounter && ((counter & 0xff000000) != 0)) {
      throw new IllegalArgumentException(
        "The counter must be between 0 and 16777215 (it must fit in three bytes).");
    }
    this.timestamp = timestamp;
    this.machineIdentifier = machineIdentifier;
    this.processIdentifier = processIdentifier;
    this.counter = counter & LOW_ORDER_THREE_BYTES;
  }

  public ObjectId(final String hexString) {
    this(parseHexString(hexString));
  }

  public ObjectId(final byte[] bytes) {
    this(ByteBuffer.wrap(bytes));
  }

  ObjectId(final int timestamp, final int machineAndProcessIdentifier, final int counter) {
    this(legacyToBytes(timestamp, machineAndProcessIdentifier, counter));
  }

  public ObjectId(final ByteBuffer buffer) {
    timestamp = makeInt(buffer.get(), buffer.get(), buffer.get(), buffer.get());
    machineIdentifier = makeInt((byte) 0, buffer.get(), buffer.get(), buffer.get());
    processIdentifier = (short) makeInt((byte) 0, (byte) 0, buffer.get(), buffer.get());
    counter = makeInt((byte) 0, buffer.get(), buffer.get(), buffer.get());
  }

  private static byte[] legacyToBytes(final int timestamp, final int machineAndProcessIdentifier,
    final int counter) {
    byte[] bytes = new byte[12];
    bytes[0] = int3(timestamp);
    bytes[1] = int2(timestamp);
    bytes[2] = int1(timestamp);
    bytes[3] = int0(timestamp);
    bytes[4] = int3(machineAndProcessIdentifier);
    bytes[5] = int2(machineAndProcessIdentifier);
    bytes[6] = int1(machineAndProcessIdentifier);
    bytes[7] = int0(machineAndProcessIdentifier);
    bytes[8] = int3(counter);
    bytes[9] = int2(counter);
    bytes[10] = int1(counter);
    bytes[11] = int0(counter);
    return bytes;
  }

  public byte[] toByteArray() {
    ByteBuffer buffer = ByteBuffer.allocate(12);
    putToByteBuffer(buffer);
    return buffer.array(); // using .allocate ensures there is a backing array that can be
  }

  public void putToByteBuffer(final ByteBuffer buffer) {

    buffer.put(int3(timestamp));
    buffer.put(int2(timestamp));
    buffer.put(int1(timestamp));
    buffer.put(int0(timestamp));
    buffer.put(int2(machineIdentifier));
    buffer.put(int1(machineIdentifier));
    buffer.put(int0(machineIdentifier));
    buffer.put(short1(processIdentifier));
    buffer.put(short0(processIdentifier));
    buffer.put(int2(counter));
    buffer.put(int1(counter));
    buffer.put(int0(counter));
  }

  public int getTimestamp() {
    return timestamp;
  }

  public int getMachineIdentifier() {
    return machineIdentifier;
  }

  public short getProcessIdentifier() {
    return processIdentifier;
  }

  public int getCounter() {
    return counter;
  }

  public Date getDate() {
    return new Date(timestamp * 1000L);
  }

  @JsonValue
  public String toHexString() {
    char[] chars = new char[24];
    int i = 0;
    for (byte b : toByteArray()) {
      chars[i++] = HEX_CHARS[b >> 4 & 0xF];
      chars[i++] = HEX_CHARS[b & 0xF];
    }
    return new String(chars);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ObjectId objectId = (ObjectId) o;

    if (counter != objectId.counter) {
      return false;
    }
    if (machineIdentifier != objectId.machineIdentifier) {
      return false;
    }
    if (processIdentifier != objectId.processIdentifier) {
      return false;
    }
    if (timestamp != objectId.timestamp) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = timestamp;
    result = 31 * result + machineIdentifier;
    result = 31 * result + (int) processIdentifier;
    result = 31 * result + counter;
    return result;
  }

  @Override
  public int compareTo(final ObjectId other) {
    if (other == null) {
      throw new NullPointerException();
    }

    byte[] byteArray = toByteArray();
    byte[] otherByteArray = other.toByteArray();
    for (int i = 0; i < 12; i++) {
      if (byteArray[i] != otherByteArray[i]) {
        return ((byteArray[i] & 0xff) < (otherByteArray[i] & 0xff)) ? -1 : 1;
      }
    }
    return 0;
  }

  @Override
  public String toString() {
    return toHexString();
  }

  static {
    try {
      MACHINE_IDENTIFIER = createMachineIdentifier();
      PROCESS_IDENTIFIER = createProcessIdentifier();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static int createMachineIdentifier() {
    // build a 2-byte machine piece based on NICs info
    int machinePiece;
    try {
      StringBuilder sb = new StringBuilder();
      Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
      while (e.hasMoreElements()) {
        NetworkInterface ni = e.nextElement();
        sb.append(ni.toString());
        byte[] mac = ni.getHardwareAddress();
        if (mac != null) {
          ByteBuffer bb = ByteBuffer.wrap(mac);
          try {
            sb.append(bb.getChar());
            sb.append(bb.getChar());
            sb.append(bb.getChar());
          } catch (BufferUnderflowException shortHardwareAddressException) { // NOPMD
            // mac with less than 6 bytes. continue
          }
        }
      }
      machinePiece = sb.toString().hashCode();
    } catch (Throwable t) {
      // exception sometimes happens with IBM JVM, use random
      machinePiece = new SecureRandom().nextInt();
    }
    machinePiece = machinePiece & LOW_ORDER_THREE_BYTES;
    return machinePiece;
  }

  // Creates the process identifier.  This does not have to be unique per class loader because
  // NEXT_COUNTER will provide the uniqueness.
  private static short createProcessIdentifier() {
    short processId;
    try {
      String processName = java.lang.management.ManagementFactory.getRuntimeMXBean()
        .getName();
      if (processName.contains("@")) {
        processId = (short) Integer.parseInt(
          processName.substring(0, processName.indexOf('@')));
      } else {
        processId = (short) java.lang.management.ManagementFactory.getRuntimeMXBean()
          .getName()
          .hashCode();
      }
    } catch (Throwable t) {
      processId = (short) new SecureRandom().nextInt();
    }

    return processId;
  }

  private static byte[] parseHexString(final String s) {
    if (!isValid(s)) {
      throw new IllegalArgumentException(
        "invalid hexadecimal representation of an ObjectId: [" + s + "]");
    }

    byte[] b = new byte[12];
    for (int i = 0; i < b.length; i++) {
      b[i] = (byte) Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16);
    }
    return b;
  }

  private static int dateToTimestampSeconds(final Date time) {
    return (int) (time.getTime() / 1000);
  }

  private static int makeInt(final byte b3, final byte b2, final byte b1, final byte b0) {
    return (b3 << 24)
      | ((b2 & 0xff) << 16)
      | ((b1 & 0xff) << 8)
      | ((b0 & 0xff));
  }

  private static byte int3(final int x) {
    return (byte) (x >> 24);
  }

  private static byte int2(final int x) {
    return (byte) (x >> 16);
  }

  private static byte int1(final int x) {
    return (byte) (x >> 8);
  }

  private static byte int0(final int x) {
    return (byte) x;
  }

  private static byte short1(final short x) {
    return (byte) (x >> 8);
  }

  private static byte short0(final short x) {
    return (byte) x;
  }
}
