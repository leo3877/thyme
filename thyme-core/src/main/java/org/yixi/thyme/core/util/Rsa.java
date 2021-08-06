package org.yixi.thyme.core.util;

import org.yixi.thyme.core.ex.ForbiddenException;
import org.yixi.thyme.core.ex.ThymeException;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import org.apache.commons.codec.binary.Base64;

/**
 * @author yixi
 * @since 1.0.0
 */
public abstract class Rsa {

  private static final Base64 base64 = new Base64();

  public static RSAPrivateKey loadPrivateKey(String privateKeyStr) {
    if (privateKeyStr == null) {
      return null;
    }
    try {
      return (RSAPrivateKey) KeyFactory.getInstance("RSA")
        .generatePrivate(new PKCS8EncodedKeySpec(base64.decode(privateKeyStr)));
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new ThymeException(e.getMessage());
    }
  }

  public static RSAPublicKey loadPublicKey(String publicKeyStr) {
    if (publicKeyStr == null) {
      return null;
    }
    try {
      return (RSAPublicKey) KeyFactory.getInstance("RSA")
        .generatePublic(new X509EncodedKeySpec(base64.decode(publicKeyStr)));
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new ThymeException(e.getMessage());
    }
  }

  /**
   * 生成公/私钥对
   */
  public static KeyPair genRsaKeyPair() {
    KeyPairGenerator keyPairGen = null;
    try {
      keyPairGen = KeyPairGenerator.getInstance("RSA");
    } catch (NoSuchAlgorithmException e) {
      throw new ThymeException(e.getMessage());
    }
    keyPairGen.initialize(1024, new SecureRandom());
    java.security.KeyPair keyPair = keyPairGen.generateKeyPair();
    return new KeyPair(Codecs.encodeBase64ToString(keyPair.getPublic().getEncoded()),
      Codecs.encodeBase64ToString(keyPair.getPrivate().getEncoded()));
  }

  /**
   * Base64 编码的公钥/私钥对
   *
   * @author yixi
   * @since 1.0.0
   */
  public static class KeyPair {

    private final String privateKey;
    private final String publicKey;

    public KeyPair(String publicKey, String privateKey) {
      this.publicKey = publicKey;
      this.privateKey = privateKey;
    }

    public String getPrivateKey() {
      return privateKey;
    }

    public String getPublicKey() {
      return publicKey;
    }
  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  public static class Signature {

    public static String sign(String privateKey, String data) {
      return sign(loadPrivateKey(privateKey), data);
    }

    public static String sign(PrivateKey privateKey, String data) {
      try {
        return base64.encodeToString(sign(privateKey, data.getBytes("utf-8")));
      } catch (UnsupportedEncodingException e) {
        throw new ForbiddenException(e.getMessage());
      }
    }

    public static byte[] sign(PrivateKey privateKey, byte[] bytes) {
      try {
        java.security.Signature signature = java.security.Signature.getInstance(
          "SHA1withRSA");
        signature.initSign(privateKey);
        signature.update(bytes);

        return signature.sign();
      } catch (Exception e) {
        throw new ForbiddenException(e.getMessage());
      }
    }

    public static boolean verify(String publicKey, String data, String base64Sign) {
      return verify(loadPublicKey(publicKey), data, base64Sign);
    }

    public static boolean verify(PublicKey publicKey, String data, String base64Sign) {
      try {
        java.security.Signature signature = java.security.Signature.getInstance(
          "SHA1withRSA");
        signature.initVerify(publicKey);
        signature.update(data.getBytes("utf-8"));
        return signature.verify(base64.decode(base64Sign));
      } catch (Exception e) {
        throw new ForbiddenException("RSA 验签[data = " + data
          + "; signature = " + base64Sign + "] " + e.getMessage());
      }
    }

    public static boolean verify(PublicKey publicKey, byte[] bytes, byte[] sign) {
      try {
        java.security.Signature signature = java.security.Signature.getInstance(
          "SHA1withRSA");
        signature.initVerify(publicKey);
        signature.update(bytes);
        return signature.verify(sign);
      } catch (Exception e) {
        throw new ForbiddenException("RSA 验签[data = " + new String(bytes)
          + "; signature = " + base64.encodeToString(sign) + "] " + e.getMessage());
      }
    }
  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  public static class Cipher {

    public static byte[] encrypt(Key key, String plainTextData) {
      return encrypt(key, plainTextData.getBytes());
    }

    public static byte[] encrypt(Key key, byte[] plainTextData) {
      try {
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("RSA");
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(plainTextData);
      } catch (Exception e) {
        throw new ThymeException(e.getMessage());
      }
    }

    public static String decryptToString(Key key, byte[] bytes) {
      return new String(decrypt(key, bytes));
    }

    public static byte[] decrypt(Key key, String cipherData) {
      return decrypt(key, cipherData.getBytes());
    }

    public static byte[] decrypt(Key key, byte[] cipherData) {
      try {
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("RSA");
        cipher.init(javax.crypto.Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(cipherData);
      } catch (Exception e) {
        throw new ThymeException(e.getMessage());
      }
    }
  }
}
