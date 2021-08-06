package org.yixi.thyme.core.util;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * copy from lucene
 *
 * @author lucene
 */
public final class RamUsageEstimator {

  public static final long ONE_KB = 1024L;
  public static final long ONE_MB = 1048576L;
  public static final long ONE_GB = 1073741824L;
  public static final int NUM_BYTES_BOOLEAN = 1;
  public static final int NUM_BYTES_BYTE = 1;
  public static final int NUM_BYTES_CHAR = 2;
  public static final int NUM_BYTES_SHORT = 2;
  public static final int NUM_BYTES_INT = 4;
  public static final int NUM_BYTES_FLOAT = 4;
  public static final int NUM_BYTES_LONG = 8;
  public static final int NUM_BYTES_DOUBLE = 8;
  public static final int NUM_BYTES_OBJECT_REF;
  public static final int NUM_BYTES_OBJECT_HEADER;
  public static final int NUM_BYTES_ARRAY_HEADER;
  public static final int NUM_BYTES_OBJECT_ALIGNMENT;
  private static final Map<Class<?>, Integer> primitiveSizes = new IdentityHashMap();
  private static final Object theUnsafe;
  private static final Method objectFieldOffsetMethod;
  private static final EnumSet<RamUsageEstimator.JvmFeature> supportedFeatures;

  public static final String OS_ARCH;

  private RamUsageEstimator() {
  }

  public static boolean isSupportedJVM() {
    return supportedFeatures.size() == RamUsageEstimator.JvmFeature.values().length;
  }

  public static long alignObjectSize(long size) {
    size += (long) NUM_BYTES_OBJECT_ALIGNMENT - 1L;
    return size - size % (long) NUM_BYTES_OBJECT_ALIGNMENT;
  }

  public static long sizeOf(byte[] arr) {
    return alignObjectSize((long) NUM_BYTES_ARRAY_HEADER + (long) arr.length);
  }

  public static long sizeOf(boolean[] arr) {
    return alignObjectSize((long) NUM_BYTES_ARRAY_HEADER + (long) arr.length);
  }

  public static long sizeOf(char[] arr) {
    return alignObjectSize((long) NUM_BYTES_ARRAY_HEADER + 2L * (long) arr.length);
  }

  public static long sizeOf(short[] arr) {
    return alignObjectSize((long) NUM_BYTES_ARRAY_HEADER + 2L * (long) arr.length);
  }

  public static long sizeOf(int[] arr) {
    return alignObjectSize((long) NUM_BYTES_ARRAY_HEADER + 4L * (long) arr.length);
  }

  public static long sizeOf(float[] arr) {
    return alignObjectSize((long) NUM_BYTES_ARRAY_HEADER + 4L * (long) arr.length);
  }

  public static long sizeOf(long[] arr) {
    return alignObjectSize((long) NUM_BYTES_ARRAY_HEADER + 8L * (long) arr.length);
  }

  public static long sizeOf(double[] arr) {
    return alignObjectSize((long) NUM_BYTES_ARRAY_HEADER + 8L * (long) arr.length);
  }

  public static long sizeOf(Object obj) {
    return measureObjectSize(obj);
  }

  public static long shallowSizeOf(Object obj) {
    if (obj == null) {
      return 0L;
    } else {
      Class<?> clz = obj.getClass();
      return clz.isArray() ? shallowSizeOfArray(obj) : shallowSizeOfInstance(clz);
    }
  }

  public static long shallowSizeOfInstance(Class<?> clazz) {
    if (clazz.isArray()) {
      throw new IllegalArgumentException("This method does not work with array classes.");
    } else if (clazz.isPrimitive()) {
      return (long) (Integer) primitiveSizes.get(clazz);
    } else {
      long size;
      for (size = (long) NUM_BYTES_OBJECT_HEADER; clazz != null; clazz = clazz.getSuperclass()) {
        Field[] fields = clazz.getDeclaredFields();
        Field[] arr$ = fields;
        int len$ = fields.length;

        for (int i$ = 0; i$ < len$; ++i$) {
          Field f = arr$[i$];
          if (!Modifier.isStatic(f.getModifiers())) {
            size = adjustForField(size, f);
          }
        }
      }

      return alignObjectSize(size);
    }
  }

  private static long shallowSizeOfArray(Object array) {
    long size = (long) NUM_BYTES_ARRAY_HEADER;
    int len = Array.getLength(array);
    if (len > 0) {
      Class<?> arrayElementClazz = array.getClass().getComponentType();
      if (arrayElementClazz.isPrimitive()) {
        size += (long) len * (long) (Integer) primitiveSizes.get(arrayElementClazz);
      } else {
        size += (long) NUM_BYTES_OBJECT_REF * (long) len;
      }
    }

    return alignObjectSize(size);
  }

  private static long measureObjectSize(Object root) {
    RamUsageEstimator.IdentityHashSet<Object> seen = new RamUsageEstimator.IdentityHashSet();
    IdentityHashMap<Class<?>, RamUsageEstimator.ClassCache> classCache = new IdentityHashMap();
    ArrayList<Object> stack = new ArrayList();
    stack.add(root);
    long totalSize = 0L;

    while (true) {
      while (true) {
        Object ob;
        do {
          do {
            if (stack.isEmpty()) {
              seen.clear();
              stack.clear();
              classCache.clear();
              return totalSize;
            }

            ob = stack.remove(stack.size() - 1);
          } while (ob == null);
        } while (seen.contains(ob));

        seen.add(ob);
        Class<?> obClazz = ob.getClass();
        int len$;
        Object o;
        if (obClazz.isArray()) {
          long size = (long) NUM_BYTES_ARRAY_HEADER;
          len$ = Array.getLength(ob);
          if (len$ > 0) {
            Class<?> componentClazz = obClazz.getComponentType();
            if (componentClazz.isPrimitive()) {
              size += (long) len$ * (long) (Integer) primitiveSizes.get(componentClazz);
            } else {
              size += (long) NUM_BYTES_OBJECT_REF * (long) len$;
              int i = len$;

              while (true) {
                --i;
                if (i < 0) {
                  break;
                }

                o = Array.get(ob, i);
                if (o != null && !seen.contains(o)) {
                  stack.add(o);
                }
              }
            }
          }

          totalSize += alignObjectSize(size);
        } else {
          try {
            RamUsageEstimator.ClassCache cachedInfo = (RamUsageEstimator.ClassCache) classCache
              .get(obClazz);
            if (cachedInfo == null) {
              classCache.put(obClazz, cachedInfo = createCacheEntry(obClazz));
            }

            Field[] arr$ = cachedInfo.referenceFields;
            len$ = arr$.length;

            for (int i$ = 0; i$ < len$; ++i$) {
              Field f = arr$[i$];
              o = f.get(ob);
              if (o != null && !seen.contains(o)) {
                stack.add(o);
              }
            }

            totalSize += cachedInfo.alignedShallowInstanceSize;
          } catch (IllegalAccessException var14) {
            throw new RuntimeException("Reflective field access failed?", var14);
          }
        }
      }
    }
  }

  private static RamUsageEstimator.ClassCache createCacheEntry(Class<?> clazz) {
    long shallowInstanceSize = (long) NUM_BYTES_OBJECT_HEADER;
    ArrayList<Field> referenceFields = new ArrayList(32);

    for (Class c = clazz; c != null; c = c.getSuperclass()) {
      Field[] fields = c.getDeclaredFields();
      Field[] arr$ = fields;
      int len$ = fields.length;

      for (int i$ = 0; i$ < len$; ++i$) {
        Field f = arr$[i$];
        if (!Modifier.isStatic(f.getModifiers())) {
          shallowInstanceSize = adjustForField(shallowInstanceSize, f);
          if (!f.getType().isPrimitive()) {
            f.setAccessible(true);
            referenceFields.add(f);
          }
        }
      }
    }

    RamUsageEstimator.ClassCache cachedInfo = new RamUsageEstimator.ClassCache(
      alignObjectSize(shallowInstanceSize),
      (Field[]) referenceFields.toArray(new Field[referenceFields.size()]));
    return cachedInfo;
  }

  private static long adjustForField(long sizeSoFar, Field f) {
    Class<?> type = f.getType();
    int fsize = type.isPrimitive() ? (Integer) primitiveSizes.get(type) : NUM_BYTES_OBJECT_REF;
    if (objectFieldOffsetMethod != null) {
      try {
        long offsetPlusSize =
          ((Number) objectFieldOffsetMethod.invoke(theUnsafe, f)).longValue() + (long) fsize;
        return Math.max(sizeSoFar, offsetPlusSize);
      } catch (IllegalAccessException var7) {
        throw new RuntimeException("Access problem with sun.misc.Unsafe", var7);
      } catch (InvocationTargetException var8) {
        Throwable cause = var8.getCause();
        if (cause instanceof RuntimeException) {
          throw (RuntimeException) cause;
        } else if (cause instanceof Error) {
          throw (Error) cause;
        } else {
          throw new RuntimeException(
            "Call to Unsafe's objectFieldOffset() throwed checked Exception when accessing field "
              + f.getDeclaringClass().getName() + "#" + f.getName(), cause);
        }
      }
    } else {
      return sizeSoFar + (long) fsize;
    }
  }

  public static EnumSet<RamUsageEstimator.JvmFeature> getUnsupportedFeatures() {
    EnumSet<RamUsageEstimator.JvmFeature> unsupported = EnumSet
      .allOf(RamUsageEstimator.JvmFeature.class);
    unsupported.removeAll(supportedFeatures);
    return unsupported;
  }

  public static EnumSet<RamUsageEstimator.JvmFeature> getSupportedFeatures() {
    return EnumSet.copyOf(supportedFeatures);
  }

  public static String humanReadableUnits(long bytes) {
    return humanReadableUnits(bytes,
      new DecimalFormat("0.#", DecimalFormatSymbols.getInstance(Locale.ROOT)));
  }

  public static String humanReadableUnits(long bytes, DecimalFormat df) {
    if (bytes / 1073741824L > 0L) {
      return df.format((double) ((float) bytes / 1.07374182E9F)) + " GB";
    } else if (bytes / 1048576L > 0L) {
      return df.format((double) ((float) bytes / 1048576.0F)) + " MB";
    } else {
      return bytes / 1024L > 0L ? df.format((double) ((float) bytes / 1024.0F)) + " KB"
        : bytes + " bytes";
    }
  }

  public static String humanSizeOf(Object object) {
    return humanReadableUnits(sizeOf(object));
  }

  static {
    boolean is64Bit = false;
    OS_ARCH = System.getProperty("os.arch");
    try {
      Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
      Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
      unsafeField.setAccessible(true);
      Object unsafe = unsafeField.get((Object) null);
      int addressSize = ((Number) unsafeClass.getMethod("addressSize").invoke(unsafe)).intValue();
      is64Bit = addressSize >= 8;
    } catch (Exception var7) {
      String x = System.getProperty("sun.arch.data.model");
      if (x != null) {
        is64Bit = x.indexOf("64") != -1;
      } else if (OS_ARCH != null && OS_ARCH.indexOf("64") != -1) {
        is64Bit = true;
      } else {
        is64Bit = false;
      }
    }

    primitiveSizes.put(Boolean.TYPE, 1);
    primitiveSizes.put(Byte.TYPE, 1);
    primitiveSizes.put(Character.TYPE, 2);
    primitiveSizes.put(Short.TYPE, 2);
    primitiveSizes.put(Integer.TYPE, 4);
    primitiveSizes.put(Float.TYPE, 4);
    primitiveSizes.put(Double.TYPE, 8);
    primitiveSizes.put(Long.TYPE, 8);
    int referenceSize = is64Bit ? 8 : 4;
    supportedFeatures = EnumSet.noneOf(RamUsageEstimator.JvmFeature.class);
    Class<?> unsafeClass = null;
    Object tempTheUnsafe = null;

    try {
      unsafeClass = Class.forName("sun.misc.Unsafe");
      Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
      unsafeField.setAccessible(true);
      tempTheUnsafe = unsafeField.get((Object) null);
    } catch (Exception var16) {
    }

    theUnsafe = tempTheUnsafe;

    Method tempObjectFieldOffsetMethod;
    try {
      tempObjectFieldOffsetMethod = unsafeClass.getMethod("arrayIndexScale", Class.class);
      referenceSize = ((Number) tempObjectFieldOffsetMethod.invoke(theUnsafe, Object[].class))
        .intValue();
      supportedFeatures.add(RamUsageEstimator.JvmFeature.OBJECT_REFERENCE_SIZE);
    } catch (Exception var15) {
    }

    int objectHeader = is64Bit ? 8 + referenceSize : 8;
    int arrayHeader = is64Bit ? 8 + 2 * referenceSize : 12;
    tempObjectFieldOffsetMethod = null;

    Method arrayBaseOffsetM;
    try {
      arrayBaseOffsetM = unsafeClass.getMethod("objectFieldOffset", Field.class);
      Field dummy1Field = RamUsageEstimator.DummyTwoLongObject.class.getDeclaredField("dummy1");
      int ofs1 = ((Number) arrayBaseOffsetM.invoke(theUnsafe, dummy1Field)).intValue();
      Field dummy2Field = RamUsageEstimator.DummyTwoLongObject.class.getDeclaredField("dummy2");
      int ofs2 = ((Number) arrayBaseOffsetM.invoke(theUnsafe, dummy2Field)).intValue();
      if (Math.abs(ofs2 - ofs1) == 8) {
        Field baseField = RamUsageEstimator.DummyOneFieldObject.class.getDeclaredField("base");
        objectHeader = ((Number) arrayBaseOffsetM.invoke(theUnsafe, baseField)).intValue();
        supportedFeatures.add(RamUsageEstimator.JvmFeature.FIELD_OFFSETS);
        tempObjectFieldOffsetMethod = arrayBaseOffsetM;
      }
    } catch (Exception var14) {
    }

    objectFieldOffsetMethod = tempObjectFieldOffsetMethod;

    try {
      arrayBaseOffsetM = unsafeClass.getMethod("arrayBaseOffset", Class.class);
      arrayHeader = ((Number) arrayBaseOffsetM.invoke(theUnsafe, byte[].class)).intValue();
      supportedFeatures.add(RamUsageEstimator.JvmFeature.ARRAY_HEADER_SIZE);
    } catch (Exception var13) {
    }

    NUM_BYTES_OBJECT_REF = referenceSize;
    NUM_BYTES_OBJECT_HEADER = objectHeader;
    NUM_BYTES_ARRAY_HEADER = arrayHeader;
    int objectAlignment = 8;

    try {
      Class<?> beanClazz = Class.forName("com.sun.management.HotSpotDiagnosticMXBean");
      Object hotSpotBean = ManagementFactory
        .newPlatformMXBeanProxy(ManagementFactory.getPlatformMBeanServer(),
          "com.sun.management:type=HotSpotDiagnostic", beanClazz);
      Method getVMOptionMethod = beanClazz.getMethod("getVMOption", String.class);
      Object vmOption = getVMOptionMethod.invoke(hotSpotBean, "ObjectAlignmentInBytes");
      objectAlignment = Integer
        .parseInt(vmOption.getClass().getMethod("getValue").invoke(vmOption).toString());
      supportedFeatures.add(RamUsageEstimator.JvmFeature.OBJECT_ALIGNMENT);
    } catch (Exception var12) {
    }

    NUM_BYTES_OBJECT_ALIGNMENT = objectAlignment;
  }

  static final class IdentityHashSet<KType> implements Iterable<KType> {

    public static final float DEFAULT_LOAD_FACTOR = 0.75F;
    public static final int MIN_CAPACITY = 4;
    public Object[] keys;
    public int assigned;
    public final float loadFactor;
    private int resizeThreshold;

    public IdentityHashSet() {
      this(16, 0.75F);
    }

    public IdentityHashSet(int initialCapacity) {
      this(initialCapacity, 0.75F);
    }

    public IdentityHashSet(int initialCapacity, float loadFactor) {
      initialCapacity = Math.max(4, initialCapacity);

      assert initialCapacity > 0 : "Initial capacity must be between (0, 2147483647].";

      assert loadFactor > 0.0F && loadFactor < 1.0F : "Load factor must be between (0, 1).";

      this.loadFactor = loadFactor;
      this.allocateBuffers(this.roundCapacity(initialCapacity));
    }

    public boolean add(KType e) {
      assert e != null : "Null keys not allowed.";

      if (this.assigned >= this.resizeThreshold) {
        this.expandAndRehash();
      }

      int mask = this.keys.length - 1;

      int slot;
      Object existing;
      for (slot = rehash(e) & mask; (existing = this.keys[slot]) != null; slot = slot + 1 & mask) {
        if (e == existing) {
          return false;
        }
      }

      ++this.assigned;
      this.keys[slot] = e;
      return true;
    }

    public boolean contains(KType e) {
      int mask = this.keys.length - 1;

      Object existing;
      for (int slot = rehash(e) & mask; (existing = this.keys[slot]) != null;
        slot = slot + 1 & mask) {
        if (e == existing) {
          return true;
        }
      }

      return false;
    }

    private static int rehash(Object o) {
      int k = System.identityHashCode(o);
      k ^= k >>> 16;
      k *= -2048144789;
      k ^= k >>> 13;
      k *= -1028477387;
      k ^= k >>> 16;
      return k;
    }

    private void expandAndRehash() {
      Object[] oldKeys = this.keys;

      assert this.assigned >= this.resizeThreshold;

      this.allocateBuffers(this.nextCapacity(this.keys.length));
      int mask = this.keys.length - 1;

      for (int i = 0; i < oldKeys.length; ++i) {
        Object key = oldKeys[i];
        if (key != null) {
          int slot;
          for (slot = rehash(key) & mask; this.keys[slot] != null; slot = slot + 1 & mask) {
          }

          this.keys[slot] = key;
        }
      }

      Arrays.fill(oldKeys, (Object) null);
    }

    private void allocateBuffers(int capacity) {
      this.keys = new Object[capacity];
      this.resizeThreshold = (int) ((float) capacity * 0.75F);
    }

    protected int nextCapacity(int current) {
      assert current > 0 && Long.bitCount((long) current) == 1 : "Capacity must be a power of two.";

      assert current << 1 > 0 : "Maximum capacity exceeded (1073741824).";

      if (current < 2) {
        current = 2;
      }

      return current << 1;
    }

    protected int roundCapacity(int requestedCapacity) {
      if (requestedCapacity > 1073741824) {
        return 1073741824;
      } else {
        int capacity;
        for (capacity = 4; capacity < requestedCapacity; capacity <<= 1) {
        }

        return capacity;
      }
    }

    public void clear() {
      this.assigned = 0;
      Arrays.fill(this.keys, (Object) null);
    }

    public int size() {
      return this.assigned;
    }

    public boolean isEmpty() {
      return this.size() == 0;
    }

    @Override
    public Iterator<KType> iterator() {
      return new Iterator<KType>() {
        int pos = -1;
        Object nextElement = this.fetchNext();

        @Override
        public boolean hasNext() {
          return this.nextElement != null;
        }

        @Override
        public KType next() {
          Object r = this.nextElement;
          if (r == null) {
            throw new NoSuchElementException();
          } else {
            this.nextElement = this.fetchNext();
            return (KType) r;
          }
        }

        private Object fetchNext() {
          ++this.pos;

          while (this.pos < IdentityHashSet.this.keys.length
            && IdentityHashSet.this.keys[this.pos] == null) {
            ++this.pos;
          }

          return this.pos >= IdentityHashSet.this.keys.length ? null
            : IdentityHashSet.this.keys[this.pos];
        }

        @Override
        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }
  }

  private static final class DummyTwoLongObject {

    public long dummy1;
    public long dummy2;

    private DummyTwoLongObject() {
    }
  }

  private static final class DummyOneFieldObject {

    public byte base;

    private DummyOneFieldObject() {
    }
  }

  private static final class ClassCache {

    public final long alignedShallowInstanceSize;
    public final Field[] referenceFields;

    public ClassCache(long alignedShallowInstanceSize, Field[] referenceFields) {
      this.alignedShallowInstanceSize = alignedShallowInstanceSize;
      this.referenceFields = referenceFields;
    }
  }

  public static enum JvmFeature {
    OBJECT_REFERENCE_SIZE("Object reference size estimated using array index scale"),
    ARRAY_HEADER_SIZE("Array header size estimated using array based offset"),
    FIELD_OFFSETS("Shallow instance size based on field offsets"),
    OBJECT_ALIGNMENT("Object alignment retrieved from HotSpotDiagnostic MX bean");

    public final String description;

    private JvmFeature(String description) {
      this.description = description;
    }

    @Override
    public String toString() {
      return super.name() + " (" + this.description + ")";
    }
  }
}
