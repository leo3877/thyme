package org.yixi.thyme.core.util;

import com.google.common.hash.Hashing;
import org.yixi.thyme.core.ex.ThymeException;
import java.security.Key;
import java.util.Base64;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.codec.net.URLCodec;

/**
 * @author yixi
 * @since 1.0.0
 */
@SuppressWarnings("all")
public abstract class Codecs {

  private static final URLCodec urlCodec = new URLCodec();

  public static String encodeHexString(byte[] bytes) {
    return Hex.encodeHexString(bytes);
  }

  public static String encodeHexString(byte[] bytes, boolean toLowerCase) {
    return Hex.encodeHexString(bytes, toLowerCase);
  }

  public static byte[] encodeBase64(byte[] bytes) {
    return Base64.getEncoder().encode(bytes);
  }

  public static String encodeBase64ToString(byte[] bytes) {
    return Base64.getEncoder().encodeToString(bytes);
  }

  public static byte[] encodeBase64URLSafe(byte[] bytes) {
    return Base64.getUrlEncoder().encode(bytes);
  }

  public static String encodeBase64URLSafeString(byte[] bytes) {
    return Base64.getUrlEncoder().encodeToString(bytes);
  }

  public static byte[] decodeBase64(byte[] bytes) {
    return Base64.getDecoder().decode(bytes);
  }

  public static byte[] decodeBase64(String base64String) {
    return Base64.getDecoder().decode(base64String.getBytes());
  }

  public static byte[] decodeHexSring(String hexString) {
    try {
      return Hex.decodeHex(hexString);
    } catch (DecoderException e) {
      throw new ThymeException(e.getMessage(), e);
    }
  }

  public static byte[] md5(byte[] bytes) {
    return Hashing.md5().hashBytes(bytes).asBytes();
  }

  public static String md5ToHexString(byte[] bytes) {
    return md5ToHexString(bytes, true);
  }

  public static String md5ToHexString(String p) {
    return md5ToHexString(p.getBytes());
  }

  public static String md5ToHexString(byte[] bytes, boolean toLowerCase) {
    return encodeHexString(md5(bytes), toLowerCase);
  }

  public static Long md5HashCode(byte[] bytes) {
    return Hashing.md5().hashBytes(bytes).asLong();
  }

  public static Long md5HashCode(String p) {
    return md5HashCode(p.getBytes());
  }

  /**
   * Generates a libc6 crypt() compatible "$1$" hash value.
   */
  public static String md5Crypt(byte[] bytes) {
    return Md5Crypt.md5Crypt(bytes);
  }

  public static String md5Crypt(byte[] bytes, String salt) {
    return Md5Crypt.md5Crypt(bytes, salt);
  }

  public static Integer crc32(byte[] bytes) {
    return Hashing.crc32().hashBytes(bytes).asInt();
  }

  public static Integer murmur3_32(byte[] bytes) {
    return Hashing.murmur3_32().hashBytes(bytes).asInt();
  }

  public static Long murmur3_128ToLong(byte[] bytes) {
    return Hashing.murmur3_128().hashBytes(bytes).asLong();
  }

  public static byte[] murmur3_128(byte[] bytes) {
    return Hashing.murmur3_128().hashBytes(bytes).asBytes();
  }

  public static byte[] sha1(byte[] bytes) {
    return Hashing.sha1().hashBytes(bytes).asBytes();
  }

  public static String sha1ToHexString(byte[] bytes) {
    return encodeHexString(sha1(bytes));
  }

  public static String sha1ToHexString(String p) {
    return sha1ToHexString(p.getBytes());
  }

  public static byte[] sha256(byte[] bytes) {
    return Hashing.sha256().hashBytes(bytes).asBytes();
  }

  public static String sha256ToHexString(byte[] bytes) {
    return encodeHexString(sha256(bytes));
  }

  public static String sha256ToHexString(String p) {
    return sha256ToHexString(p.getBytes());
  }

  public static byte[] sha384(byte[] bytes) {
    return Hashing.sha384().hashBytes(bytes).asBytes();
  }

  public static byte[] sha512(byte[] bytes) {
    return Hashing.sha512().hashBytes(bytes).asBytes();
  }

  public static byte[] hmacMd5(byte[] keys, byte[] values) {
    return Hashing.hmacMd5(keys).hashBytes(values).asBytes();
  }

  public static byte[] hmacMd5(Key key, byte[] values) {
    return Hashing.hmacMd5(key).hashBytes(values).asBytes();
  }

  public static byte[] hmacSha1(byte[] keys, byte[] values) {
    return Hashing.hmacSha1(keys).hashBytes(values).asBytes();
  }

  public static byte[] hmacSha1(Key key, byte[] values) {
    return Hashing.hmacSha1(key).hashBytes(values).asBytes();
  }

  public static byte[] hmacSha256(byte[] keys, byte[] values) {
    return Hashing.hmacSha256(keys).hashBytes(values).asBytes();
  }

  public static byte[] hmacSha256(Key key, byte[] values) {
    return Hashing.hmacSha256(key).hashBytes(values).asBytes();
  }

  public static byte[] hmacSha512(byte[] keys, byte[] values) {
    return Hashing.hmacSha512(keys).hashBytes(values).asBytes();
  }

  public static byte[] hmacSha512(Key key, byte[] values) {
    return Hashing.hmacSha512(key).hashBytes(values).asBytes();
  }

  public static String encodeUrlToString(byte[] bytes) {
    return new String(encodeUrl(bytes));
  }

  public static String encodeUrlToString(String urlString) {
    return encodeUrlToString(urlString.getBytes());
  }

  public static byte[] encodeUrl(byte[] bytes) {
    return urlCodec.encode(bytes);
  }

  public static String decodeUrlToString(String urlString) {
    return decodeUrlToString(urlString);
  }

  public static String decodeUrlToString(byte[] bytes) {
    return new String(decodeUrl(bytes));
  }

  public static byte[] decodeUrl(byte[] bytes) {
    try {
      return urlCodec.decode(bytes);
    } catch (DecoderException e) {
      throw new ThymeException(e.getMessage(), e);
    }
  }
}
