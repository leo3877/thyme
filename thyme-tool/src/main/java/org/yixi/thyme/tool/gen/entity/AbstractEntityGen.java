package org.yixi.thyme.tool.gen.entity;

import com.google.common.base.CaseFormat;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yixi
 * @since 1.0.0
 */
@SuppressWarnings("all")
public abstract class AbstractEntityGen implements EntityGen {

  private static final Logger logger = LoggerFactory.getLogger(AbstractEntityGen.class);

  private final String packageName;
  private final String basedir;
  private final String author;

  public AbstractEntityGen(String packageName, String basedir, String author) {
    this.packageName = packageName;
    this.basedir = basedir;
    this.author = author;
  }

  protected void gen(EntitySchema entitySchema, Profile profile) {
    Set<String> imports = new HashSet<>();
    Map<String, String> fields = new HashMap<>();
    if (profile.isDomain()) {
      imports.add("import BaseEntity;\n");
    }
    if (profile == Profile.MongoDomain) {
      imports.add("import javax.persistence.Table;");
    }
    entitySchema.getFields().forEach((k, v) -> {
      if (profile.isDomain() && ("id".equals(k)
        || "createdTime".equals(k)
        || "updatedTime".equals(k))) {
        return;
      }
      StringBuilder sb = new StringBuilder();
      if (!v.isNullable()) {
        sb.append("\t").append("@NotNull\n");
        imports.add("import javax.validation.constraints.NotNull;\n");
      }
      if (v.isUnsigned()) {
        sb.append("\t").append("@Min(0)\n");
        imports.add("import javax.validation.constraints.Min;\n");
      }
      if (v.getMaxSize() > 0) {
        sb.append("\t").append("@Size(min = 1, max = " + v.getMaxSize() + ")\n");
        imports.add("import javax.validation.constraints.Size;\n");
      }
      String fieldName = v.getFieldName();
      if (!fieldName.equals(v.getColumnName()) && profile == Profile.MysqlDomain) {
        imports.add("import javax.persistence.Column;\n");
        sb.append("\t").append("@Column(name = \"" + v.getColumnName() + "\")\n");
      } else if (!fieldName.equals(v.getColumnName()) && profile == Profile.JsonVo) {
        imports.add("import com.fasterxml.jackson.annotation.JsonProperty;\n");
        sb.append("\t").append("@JsonProperty(\"" + v.getColumnName() + "\")\n");
      }
      if ("Date".equals(v.getType())
        || "Map".equals(v.getType())
        || "List".equals(v.getType())) {
        imports.add("import java.util." + v.getType() + ";\n");
      }
      sb.append("\t")
        .append("private ")
        .append(v.getType() + " ")
        .append(v.getFieldName() + ";");
      if (StringUtils.isNotBlank(v.getComments())) {
        sb.append(" // ").append(v.getComments());
      }
      sb.append("\n");

      fields.put(v.getFieldName(), sb.toString());
    });

    StringBuilder classBuilder = new StringBuilder();
    classBuilder.append("package " + packageName + ";\n");
    for (String importName : imports) {
      classBuilder.append(importName).append("\n");
    }
    classBuilder.append("/**\n"
      + " * @author " + author + "\n"
      + " * @since 1.0.0\n"
      + " */\n");
    if (profile == Profile.MongoDomain) {
      classBuilder.append(
        "@Table(name = " + entitySchema.getClassName() + ".COLLECTION_NAME)\n");
    }
    if (profile.isDomain()) {
      classBuilder.append(
        "public class " + entitySchema.getClassName() + " extends BaseEntity<" + (
          Profile.MysqlDomain == profile ? "Long" : "String") + "> {\n");
    } else {
      classBuilder.append(
        "public class " + entitySchema.getClassName() + " {\n");
    }
    classBuilder.append("\n");
    if (profile == Profile.MongoDomain) {
      classBuilder.append("\tpublic static final String COLLECTION_NAME = "
        + "\"" + CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,
        entitySchema.getClassName()) + "\";\n\n");
    }
    fields.forEach((k, v) -> classBuilder.append(v));
    fields.forEach((k, v) -> classBuilder.append("\n"
      + "    public " + entitySchema.getFields().get(k).getType() + " get"
      + lowerToCamel(k) + "() {\n"
      + "        return " + k + ";\n"
      + "    }\n"
      + "\n"
      + "    public void set" + lowerToCamel(k) + "("
      + entitySchema.getFields().get(k).getType() + " " + k + ") {\n"
      + "        this." + k + " = " + k + ";\n"
      + "    }\n"));
    classBuilder.append("}\n");

    try {
      FileUtils.writeStringToFile(
        new File(basedir
          + "/src/main/java/"
          + packageName.replace(".", "/")
          + "/"
          + entitySchema.getClassName()
          + ".java"),
        classBuilder.toString(), "utf-8");
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }
}


