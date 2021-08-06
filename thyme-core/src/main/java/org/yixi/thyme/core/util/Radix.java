package org.yixi.thyme.core.util;

import org.yixi.thyme.core.ex.ThymeException;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 10 进制与任意进制相互转化
 *
 * @author yixi
 * @since 1.0.0
 */
@SuppressWarnings("all")
public class Radix {

  public final static char[] DEFAULT_DIGITS = new char[]{ // 默认最大进制支持 64 位
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    , 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'
    , 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T'
    , 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd'
    , 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n'
    , 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x'
    , 'y', 'z', '_', '-'
  };

  private final Map<Character, Integer> digitMap = new HashMap<>();

  private final char[] digits;
  private final int radix;

  /**
   * @param radix 进制位数
   */
  public Radix(int radix) {
    this(Arrays.copyOf(DEFAULT_DIGITS, radix));
  }

  public Radix(String symbols) {
    this(symbols.toCharArray());
  }

  public Radix(char[] symbols) {
    this.radix = symbols.length;
    this.digits = symbols;
    int i = 0;
    for (char c : digits) {
      digitMap.put(c, i++);
    }
  }

  public String getMinValueInLength(Integer length) {
    if (length <= 0) {
      throw new ThymeException("length must not be less than 0 or equal 0");
    }
    char[] digitsChars = getDigits();
    char low = digitsChars[0];
    char second = digitsChars[1];
    char[] chars = new char[length];
    if (length == 1) {
      chars[0] = low;
    } else {
      chars[0] = second;
      for (int i = 1; i < length; i++) {
        chars[i] = low;
      }
    }
    return new String(chars);
  }

  public String getMaxValueInLength(Integer length) {
    if (length <= 0) {
      throw new ThymeException("length must not be less than 0 or equal 0");
    }
    char[] digitsChars = getDigits();
    char value = digitsChars[digitsChars.length - 1];
    char[] chars = new char[length];
    for (int i = 0; i < length; i++) {
      chars[i] = value;
    }
    return new String(chars);
  }

  public String make(Long num) {
    CharBuffer charBuffer = CharBuffer.allocate(24); // 用 charBuffer 比直接用字符串性能高一倍
    long newNum = num;
    while (newNum != 0) {
      int mod = (int) (newNum % radix);
      charBuffer.put(digits[mod]);
      newNum = newNum / radix;
    }
    charBuffer.flip();
    char[] chars = new char[charBuffer.limit()];
    for (int i = chars.length - 1; i >= 0; i--) {
      chars[chars.length - 1 - i] = charBuffer.get(i);
    }
    return new String(chars);
  }

  public Long toLong(String val) {
    Long sum = 0L;
    char[] chars = val.toCharArray();
    int i = chars.length - 1;
    for (char c : chars) {
      long pow = pow(radix, i--); // java Math.pow 函数是本地方法实现，性能太低（10 倍左右的差距）
      sum += pow * digitMap.get(c);
    }
    return sum;
  }

  public long pow(long a, long b) {
    long result = 1;
    while (b > 0) {
      if ((b & 1) == 1) {
        result *= a;
      }
      b >>= 1;
      a *= a;
    }
    return result;
  }

  public char[] getDigits() {
    return digits;
  }

  public int getRadixSize() {
    return radix;
  }
}
