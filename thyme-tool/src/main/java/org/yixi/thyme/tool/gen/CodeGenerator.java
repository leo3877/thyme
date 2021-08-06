package org.yixi.thyme.tool.gen;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;

/**
 * @author yixi
 * @since 1.0.0
 */
public class CodeGenerator {

  private final String baseUrl;
  private final String serviceName;
  private final String sourceDomainName;
  private final String targetDomainName;

  public CodeGenerator(String baseUrl, String serviceName, String sourceDomainName,
    String targetDomainName) {
    this.baseUrl = baseUrl;
    this.serviceName = serviceName;
    this.sourceDomainName = sourceDomainName;
    this.targetDomainName = targetDomainName;
  }

  public void generate() {
    new RecursiveFile(baseUrl).dirHandler(dir -> {
    }).fileHandler(file -> {
      if (file.getName().startsWith(sourceDomainName)) {
        try {
          File destFile = new File(file.getPath().replace(sourceDomainName, targetDomainName));
          if (destFile.exists()) {
            return;
          }
          FileUtils.copyFile(file, destFile);
          fileReplace(destFile, sourceDomainName, targetDomainName);
          fileReplace(destFile, toLowerCase(sourceDomainName),
            toLowerCase(targetDomainName));
        } catch (IOException e) {
          throw new IllegalArgumentException(e);
        }
      } else if (file.getName().equals(serviceName + "ClientModule.java")) {
        try {
          addBean2ClientModule(file);
        } catch (IOException e) {
          throw new IllegalArgumentException(e);
        }
      }
    }).recursive();
  }

  public void delete() {
    new RecursiveFile(baseUrl).dirHandler(dir -> {
    }).fileHandler(file -> {
      if (file.getName().startsWith(targetDomainName)) {
        try {
          FileUtils.forceDelete(file);
        } catch (IOException e) {
          throw new IllegalArgumentException(e);
        }
      }
    }).recursive();
  }

  private void fileReplace(File file, CharSequence target, CharSequence replacement) {
    try {
      FileUtils.write(file, FileUtils.readFileToString(file, "utf-8")
        .replace(target, replacement), "utf-8");
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private String toLowerCase(String str) {
    char c = str.charAt(0);
    if (c >= 'A' && c <= 'Z') {
      String temp = new String(new char[]{c});
      return temp.toLowerCase() + str.substring(1);
    }
    return str;
  }

  private void addBean2ClientModule(File file) throws IOException {
    String content = FileUtils.readFileToString(file, "utf-8");
    if (content.indexOf(targetDomainName) > 0) {
      return;
    }
    Pattern pattern = Pattern.compile(
      "binder\\.bind\\(ExampleService\\.class\\)\\.to\\(ExampleServiceImpl\\.class\\);");

    Matcher matcher = pattern.matcher(content);
    if (matcher.find()) {
      String group = matcher.group();
      int i = content.indexOf(group);

      content = content.substring(0, i + group.length()) + "\n\t\t"
        + "binder.bind("
        + targetDomainName
        + "Service.class).to("
        + targetDomainName
        + "ServiceImpl.class);"
        + content.substring(
        i + group.length());
    }

    FileUtils.write(file, content, "utf-8");
  }

  /**
   * @author yixi
   * @since 1.0.1
   */
  public static class RecursiveFile {

    private final String dir;

    private Consumer<File> dirHandler;
    private Consumer<File> fileHandler;

    public RecursiveFile(String dir) {
      this.dir = dir;
    }

    public RecursiveFile dirHandler(Consumer<File> dirHandler) {
      this.dirHandler = dirHandler;
      return this;
    }

    public RecursiveFile fileHandler(Consumer<File> fileHandler) {
      this.fileHandler = fileHandler;
      return this;
    }

    public void recursive() {
      doRecursive(new File(dir));
    }

    private void doRecursive(File file) {
      if (file.isDirectory()) {
        File[] files = file.listFiles();
        for (File child : files) {
          if (child.isFile()) {
            fileHandler.accept(child);
          } else if (child.isDirectory()) {
            doRecursive(child);
            dirHandler.accept(child);
          }
        }
      }
    }
  }
}
