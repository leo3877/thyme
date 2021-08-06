package org.yixi.thyme.core.util;

/**
 * java 基本类型编解码工具类
 *
 * @author yixi
 * @since 1.0.0
 */
@SuppressWarnings("all")
public abstract class Bits {

  public static byte bytes(boolean b) {
    return (byte) (b ? 1 : 0);
  }

  public static byte[] bytesB(char c) {
    byte[] bytes = new byte[2];
    bytes[1] = (byte) (c);
    bytes[0] = (byte) (c >>> 8);
    return bytes;
  }

  public static byte[] bytesL(char c) {
    byte[] bytes = new byte[2];
    bytes[0] = (byte) (c);
    bytes[1] = (byte) (c >>> 8);
    return bytes;
  }

  public static byte[] bytesB(short val) {
    byte[] bytes = new byte[2];
    bytes[1] = (byte) (val);
    bytes[0] = (byte) (val >>> 8);
    return bytes;
  }

  public static byte[] bytesL(short val) {
    byte[] bytes = new byte[2];
    bytes[0] = (byte) (val);
    bytes[1] = (byte) (val >>> 8);
    return bytes;
  }

  public static byte[] bytesB(long val) {
    byte[] bytes = new byte[8];
    bytes[7] = (byte) (val);
    bytes[6] = (byte) (val >>> 8);
    bytes[5] = (byte) (val >>> 16);
    bytes[4] = (byte) (val >>> 24);
    bytes[3] = (byte) (val >>> 32);
    bytes[2] = (byte) (val >>> 40);
    bytes[1] = (byte) (val >>> 48);
    bytes[0] = (byte) (val >>> 56);
    return bytes;
  }

  public static byte[] bytesL(long val) {
    byte[] bytes = new byte[8];
    bytes[0] = (byte) (val);
    bytes[1] = (byte) (val >>> 8);
    bytes[2] = (byte) (val >>> 16);
    bytes[3] = (byte) (val >>> 24);
    bytes[4] = (byte) (val >>> 32);
    bytes[5] = (byte) (val >>> 40);
    bytes[6] = (byte) (val >>> 48);
    bytes[7] = (byte) (val >>> 56);
    return bytes;
  }

  public static byte[] bytesB(int val) {
    byte[] bytes = new byte[4];
    bytes[3] = (byte) (val);
    bytes[2] = (byte) (val >>> 8);
    bytes[1] = (byte) (val >>> 16);
    bytes[0] = (byte) (val >>> 24);
    return bytes;
  }

  public static byte[] bytesL(int val) {
    byte[] bytes = new byte[4];
    bytes[0] = (byte) (val);
    bytes[1] = (byte) (val >>> 8);
    bytes[2] = (byte) (val >>> 16);
    bytes[3] = (byte) (val >>> 24);
    return bytes;
  }

  public static byte[] bytesL(Double val) {
    return bytesL(Double.doubleToLongBits(val));
  }

  public static byte[] bytesB(Double val) {
    return bytesB(Double.doubleToLongBits(val));
  }

  public static byte[] bytesL(Float val) {
    return bytesL(Float.floatToIntBits(val));
  }

  public static byte[] bytesB(Float val) {
    return bytesB(Float.floatToIntBits(val));
  }

  public static boolean getBoolean(byte bytes) {
    return (bytes == 1) ? true : false;
  }

  public static char getCharL(byte[] bytes) {
    return (char) (((bytes[1] & 0xff) << 8) | (bytes[0] & 0xff));
  }

  public static char getCharB(byte[] bytes) {
    return (char) (((bytes[0] & 0xff) << 8) | (bytes[1] & 0xff));
  }

  public static short getShortL(byte[] bytes) {
    return (short) (((bytes[1] & 0xff) << 8) | (bytes[0] & 0xff));
  }

  public static short getShortB(byte[] bytes) {
    return (short) (((bytes[0] & 0xff) << 8) | (bytes[1] & 0xff));
  }

  public static int getIntegerL(byte[] bytes) {
    return (((int) bytes[3]) << 24) |
      (((int) bytes[2] & 0xff) << 16) |
      (((int) bytes[1] & 0xff) << 8) |
      (((int) bytes[0] & 0xff));
  }

  public static int getIntegerB(byte[] bytes) {
    return (((int) bytes[0]) << 24) |
      (((int) bytes[1] & 0xff) << 16) |
      (((int) bytes[2] & 0xff) << 8) |
      (((int) bytes[3] & 0xff));
  }

  public static long getLongL(byte[] bytes) {
    return (((long) bytes[7]) << 56) |
      (((long) bytes[6] & 0xff) << 48) |
      (((long) bytes[5] & 0xff) << 40) |
      (((long) bytes[4] & 0xff) << 32) |
      (((long) bytes[3] & 0xff) << 24) |
      (((long) bytes[2] & 0xff) << 16) |
      (((long) bytes[1] & 0xff) << 8) |
      (((long) bytes[0] & 0xff));
  }

  public static long getLongB(byte[] bytes) {
    return (((long) bytes[0]) << 56) |
      (((long) bytes[1] & 0xff) << 48) |
      (((long) bytes[2] & 0xff) << 40) |
      (((long) bytes[3] & 0xff) << 32) |
      (((long) bytes[4] & 0xff) << 24) |
      (((long) bytes[5] & 0xff) << 16) |
      (((long) bytes[6] & 0xff) << 8) |
      ((long) bytes[7] & 0xff);
  }

  public static double getDoubleB(byte[] bytes) {
    return Double.longBitsToDouble(getLongB(bytes));
  }

  public static double getDoubleL(byte[] bytes) {
    return Double.longBitsToDouble(getLongL(bytes));
  }

  public static double getFloatB(byte[] bytes) {
    return Float.intBitsToFloat(getIntegerB(bytes));
  }

  public static double getFloatL(byte[] bytes) {
    return Float.intBitsToFloat(getIntegerL(bytes));
  }
}
