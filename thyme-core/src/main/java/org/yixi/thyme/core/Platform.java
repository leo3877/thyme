package org.yixi.thyme.core;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import org.apache.commons.lang3.SystemUtils;
import org.yixi.thyme.core.ex.ThymeException;
import org.yixi.thyme.core.json.Jsons;

/**
 * 运行时环境信息
 *
 * @author yixi
 * @since 1.0.0
 */
public class Platform {

  private static final Platform DEFAULT = new Platform();

  /**
   * 操作系统及版本
   */
  private final String osName = SystemUtils.OS_NAME;
  private final String osVersion = SystemUtils.OS_VERSION;
  private final String osArch = SystemUtils.OS_ARCH;
  private final boolean linux = SystemUtils.IS_OS_LINUX;
  private final boolean unix = SystemUtils.IS_OS_UNIX;
  private final boolean windows = SystemUtils.IS_OS_WINDOWS;
  private final boolean mac = SystemUtils.IS_OS_MAC;

  /**
   * java 版本
   */
  private final String javaSpecificationVersion = SystemUtils.JAVA_SPECIFICATION_VERSION;
  private final String javaVersion = SystemUtils.JAVA_VERSION;
  private final boolean java7 = SystemUtils.IS_JAVA_1_7;
  private final boolean java8 = SystemUtils.IS_JAVA_1_8;
  private final boolean java9 = SystemUtils.IS_JAVA_9;
  private final boolean java10 = SystemUtils.IS_JAVA_10;
  private final String javaRuntimeName = SystemUtils.JAVA_RUNTIME_NAME;
  private final String javaRuntimeVersion = SystemUtils.JAVA_RUNTIME_VERSION;

  /**
   * 文件分隔符
   */
  private final String fileSeparator = File.separator;
  /**
   * 换行符
   */
  private final String lineSeparator = System.lineSeparator();
  /**
   * 临时目录
   */
  private final String tmpDir = SystemUtils.JAVA_IO_TMPDIR;
  /**
   * 应用目录
   */
  private final String userDir = SystemUtils.USER_DIR;
  /**
   * 操作系统用户 HOME 目录
   */
  private final String userHome = SystemUtils.USER_HOME;
  /**
   * Java HOME 目录
   */
  private final String javaHome = SystemUtils.JAVA_HOME;
  /**
   * 系统默认编码
   */
  private final String fileEncoding = SystemUtils.FILE_ENCODING;

  /**
   * classpath
   */
  private final String classPath = SystemUtils.JAVA_CLASS_PATH;

  public String getOsName() {
    return osName;
  }

  public String getOsVersion() {
    return osVersion;
  }

  public String getOsArch() {
    return osArch;
  }

  public boolean isLinux() {
    return linux;
  }

  public boolean isUnix() {
    return unix;
  }

  public boolean isWindows() {
    return windows;
  }

  public String getJavaSpecificationVersion() {
    return javaSpecificationVersion;
  }

  public String getJavaVersion() {
    return javaVersion;
  }

  public boolean isJava7() {
    return java7;
  }

  public boolean isJava8() {
    return java8;
  }

  public boolean isJava9() {
    return java9;
  }

  public boolean isJava10() {
    return java10;
  }

  public String getFileSeparator() {
    return fileSeparator;
  }

  public String getLineSeparator() {
    return lineSeparator;
  }

  public String getTmpDir() {
    return tmpDir;
  }

  public String getUserDir() {
    return userDir;
  }

  public String getUserHome() {
    return userHome;
  }

  public String getJavaHome() {
    return javaHome;
  }

  public String getClassPath() {
    return classPath;
  }

  public String getJavaRuntimeName() {
    return javaRuntimeName;
  }

  public String getJavaRuntimeVersion() {
    return javaRuntimeVersion;
  }

  public String getFileEncoding() {
    return fileEncoding;
  }

  public boolean isMac() {
    return mac;
  }

  public static Platform get() {
    return DEFAULT;
  }

  public String getHostName() {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      throw new ThymeException(e.getMessage(), e);
    }
  }

  public String ip() {
    String tempIP;
    try {
      tempIP = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      throw new ThymeException(e.getMessage(), e);
    }
    try {
      Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
      InetAddress ip;
      Enumeration<InetAddress> addresses;
      while (networks.hasMoreElements()) {
        NetworkInterface networkInterface = networks.nextElement();
        addresses = networkInterface.getInetAddresses();
        while (addresses.hasMoreElements()) {
          ip = addresses.nextElement();
          if (ip != null
            && ip instanceof Inet4Address
            && ip.isSiteLocalAddress()
            && !ip.getHostAddress().equals(tempIP)
            && !networkInterface.getName().equals("docker0")) {
            return ip.getHostAddress();
          }
        }
      }
      return tempIP;
    } catch (Exception e) {
      throw new ThymeException(e.getMessage(), e);
    }
  }

  @Override
  public String toString() {
    return Jsons.encode(this);
  }
}
