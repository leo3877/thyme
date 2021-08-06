package org.yixi.thyme.core.util;

import org.yixi.thyme.core.ex.ThymeException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.lang3.StringUtils;

/**
 * @author yixi
 * @since 1.0.0
 */
public class Aes {

  /**
   * 加密模式，常见模式: ECB, CBC, CFB, OFB, CTR, XTS 推荐使用 CBC
   */
  private String mode = "CBC";
  /**
   * 密钥长度，128，192，256，默认 128
   */
  private int secretKeySize = 128;
  /**
   * 密钥生成方式：md5, raw, sha1prngRandom
   */
  private String keyMechanism = "raw";
  /**
   * iv 初始偏移量, 默认16个0
   */
  private String iv = "0000000000000000";
  /**
   * 填充方式, NoPadding, ZeroPadding, PKCS5Padding, ISO10126Padding, ANSI X.923, SSL3Padding
   */
  private String padding = "PKCS5Padding";

  public String encryptHex(String content, String password) {
    return encryptHex(content.getBytes(), password);
  }

  public String encryptHex(byte[] content, String password) {
    return Codecs.encodeHexString(encrypt(content, password));
  }

  public String encryptBase64(String content, String password) {
    return encryptBase64(content.getBytes(), password);
  }

  public String encryptBase64(byte[] content, String password) {
    return Codecs.encodeBase64ToString(encrypt(content, password));
  }

  public byte[] encrypt(byte[] content, String password) {
    try {
      Cipher cipher = Cipher.getInstance("AES/ " + mode + "/" + padding);
      if ("ECB".equals(mode)) {
        cipher.init(Cipher.ENCRYPT_MODE, genSecretKey(password));
      } else {
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, genSecretKey(password), ivParameterSpec);
      }
      return cipher.doFinal(content);
    } catch (Exception ex) {
      throw new ThymeException(ex.getMessage(), ex);
    }
  }

  public byte[] decodeHexString(String ciphertext, String password) {
    return decrypt(Codecs.decodeHexSring(ciphertext), password);
  }

  public byte[] decryptBase64(String ciphertText, String password) {
    return decrypt(Codecs.decodeBase64(ciphertText), password);
  }

  public byte[] decrypt(byte[] bytes, String password) {
    try {
      Cipher cipher = Cipher.getInstance("AES/ " + mode + "/" + padding);
      if ("ECB".equals(mode)) {
        cipher.init(Cipher.DECRYPT_MODE, genSecretKey(password));
      } else {
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes());
        cipher.init(Cipher.DECRYPT_MODE, genSecretKey(password), ivParameterSpec);
      }
      return cipher.doFinal(bytes);
    } catch (Exception e) {
      throw new ThymeException(e.getMessage(), e);
    }
  }

  private SecretKeySpec genSecretKey(String password) {
    if ("raw".equals(keyMechanism)) {
      return new SecretKeySpec(password.getBytes(), "AES");
    } else if ("md5".equals(keyMechanism)) {
      return new SecretKeySpec(Codecs.md5(password.getBytes()), "AES");
    } else if ("sha1prngRandom".equals(keyMechanism)) {
      try {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(password.getBytes());
        keyGenerator.init(secretKeySize, random);
        SecretKey secretKey = keyGenerator.generateKey();
        return new SecretKeySpec(secretKey.getEncoded(), "AES");
      } catch (NoSuchAlgorithmException ex) {
        throw new ThymeException(ex.getMessage(), ex);
      }
    } else {
      throw new UnsupportedOperationException(
        "keyMechanism unsupported. keyMechanism: " + keyMechanism);
    }
  }

  /**
   * @author yixi
   */
  public static class Builder {

    private Aes aes = new Aes();

    public Builder mode(String mode) {
      if (StringUtils.isNotBlank(mode)) {
        aes.mode = mode;
      }
      return this;
    }

    public Builder secretKeySize(int secretKeySize) {
      if (secretKeySize == 128
        || secretKeySize == 192
        || secretKeySize == 256) {
        aes.secretKeySize = secretKeySize;
      } else {
        throw new UnsupportedOperationException(secretKeySize + "");
      }
      return this;
    }

    public Builder keyMechanism(String keyMechanism) {
      if ("md5".equals(keyMechanism)
        || "raw".equals(keyMechanism)
        || "sha1prngRandom".equals(keyMechanism)) {
        aes.keyMechanism = keyMechanism;
      } else {
        throw new UnsupportedOperationException(keyMechanism);
      }
      return this;
    }

    public Builder iv(String iv) {
      if (StringUtils.isNotBlank(iv)) {
        aes.iv = iv;
      }
      return this;
    }

    public Builder padding(String padding) {
      if (StringUtils.isNotBlank(padding)) {
        aes.padding = padding;
      }
      return this;
    }

    public Aes build() {
      return aes;
    }

    public static Builder builder() {
      return new Builder();
    }
  }
}

