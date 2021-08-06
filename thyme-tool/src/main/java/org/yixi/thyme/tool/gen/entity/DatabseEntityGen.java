package org.yixi.thyme.tool.gen.entity;

import org.yixi.thyme.core.ex.ThymeException;
import org.yixi.thyme.tool.gen.entity.EntityGen.ClassField.ClassFieldBuilder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yixi
 * @since 1.0.0
 */
@SuppressWarnings("all")
public class DatabseEntityGen extends AbstractEntityGen {

  private static final Logger logger = LoggerFactory.getLogger(DatabseEntityGen.class);

  public static final Set<String> CREATED_COLUMNS = new HashSet<>();
  public static final Set<String> UPDATED_COLUMNS = new HashSet<>();
  public static final Map<String, String> COLUMN_TYPE_2_ATTR_TYPE_MAPPINGS = new HashMap<>();

  static {
    CREATED_COLUMNS.add("created_time");
    CREATED_COLUMNS.add("creation_time");

    UPDATED_COLUMNS.add("updated_time");
    UPDATED_COLUMNS.add("modify_time");

    COLUMN_TYPE_2_ATTR_TYPE_MAPPINGS.put("tinyint", "Integer");
    COLUMN_TYPE_2_ATTR_TYPE_MAPPINGS.put("tinyint unsigned", "Integer");
    COLUMN_TYPE_2_ATTR_TYPE_MAPPINGS.put("smallint", "Integer");
    COLUMN_TYPE_2_ATTR_TYPE_MAPPINGS.put("smallint unsigned", "Integer");
    COLUMN_TYPE_2_ATTR_TYPE_MAPPINGS.put("mediumint", "Integer");
    COLUMN_TYPE_2_ATTR_TYPE_MAPPINGS.put("mediumint unsigned", "Integer");
    COLUMN_TYPE_2_ATTR_TYPE_MAPPINGS.put("int", "Integer");
    COLUMN_TYPE_2_ATTR_TYPE_MAPPINGS.put("int unsigned", "Long");
    COLUMN_TYPE_2_ATTR_TYPE_MAPPINGS.put("bigint", "Long");
    COLUMN_TYPE_2_ATTR_TYPE_MAPPINGS.put("bigint unsigned", "Long");
    COLUMN_TYPE_2_ATTR_TYPE_MAPPINGS.put("decimal", "BigDecimal");
    COLUMN_TYPE_2_ATTR_TYPE_MAPPINGS.put("float", "Float");
    COLUMN_TYPE_2_ATTR_TYPE_MAPPINGS.put("float unsigned", "Float");
    COLUMN_TYPE_2_ATTR_TYPE_MAPPINGS.put("double", "Double");
    COLUMN_TYPE_2_ATTR_TYPE_MAPPINGS.put("double unsigned", "Double");

    COLUMN_TYPE_2_ATTR_TYPE_MAPPINGS.put("char", "String");
    COLUMN_TYPE_2_ATTR_TYPE_MAPPINGS.put("varchar", "String");

    COLUMN_TYPE_2_ATTR_TYPE_MAPPINGS.put("time", "Date");
    COLUMN_TYPE_2_ATTR_TYPE_MAPPINGS.put("date", "Date");
    COLUMN_TYPE_2_ATTR_TYPE_MAPPINGS.put("datetime", "Date");
    COLUMN_TYPE_2_ATTR_TYPE_MAPPINGS.put("timestamp", "Date");

    COLUMN_TYPE_2_ATTR_TYPE_MAPPINGS.put("binary", "byte[]");
    COLUMN_TYPE_2_ATTR_TYPE_MAPPINGS.put("varbinary", "byte[]");
    COLUMN_TYPE_2_ATTR_TYPE_MAPPINGS.put("tinyblob", "byte[]");
    COLUMN_TYPE_2_ATTR_TYPE_MAPPINGS.put("blob", "byte[]");
    COLUMN_TYPE_2_ATTR_TYPE_MAPPINGS.put("mediumblob", "byte[]");
    COLUMN_TYPE_2_ATTR_TYPE_MAPPINGS.put("longblob", "byte[]");
  }

  public DatabseEntityGen(String packageName, String basedir, String author) {
    super(packageName, basedir, author);
  }

  public void mysqlGen(Config config, Profile profile) {
    try {

      List<String> tables = config.getTables();
      Connection connection = connection(config.databaseUrl, config.username,
        config.password);
      if (tables == null) {
        tables = new ArrayList<>();
        ResultSet rs = connection.createStatement().executeQuery("SHOW TABLES;");
        while (rs.next()) {
          tables.add(rs.getString(1));
        }
        rs.close();
      }
      if (tables == null) {
        return;
      }

      for (String tb : tables) {
        EntitySchema entitySchema = new EntitySchema(className(tb));
        ResultSet r = connection.createStatement()
          .executeQuery("show full columns from " + tb);
        while (r.next()) {
          ClassFieldBuilder classFieldBuilder = ClassField.builder()
            .comments(r.getString("Comment"))
            .columnName(r.getString("Field"))
            .fieldName(fieldName(r.getString("Field")));
          String aNull = r.getString("Null");
          if ("NO".equals(aNull)) {
            classFieldBuilder.nullable(false);
          }
          entitySchema.addFiled(classFieldBuilder.build());
        }
        r.close();
        ResultSet rs = connection.createStatement().executeQuery("select * from " + tb);
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
          String columnName = metaData.getColumnName(i);
          ClassField classField = entitySchema.getFields().get(fieldName(columnName));
          classField.setType(fieldType(metaData, i));
          if ("String".equals(classField.getType())) {
            classField.setMaxSize(metaData.getColumnDisplaySize(i));
          } else {
            String columnType = metaData.getColumnTypeName(i).toLowerCase();
            if (columnType.indexOf("unsigned") > 0) {
              classField.setUnsigned(true);
            }
          }
        }
        rs.close();
        gen(entitySchema, profile);
      }
    } catch (Exception e) {
      throw new ThymeException(e.getMessage(), e);
    }
  }

  public String fieldType(ResultSetMetaData metaData, int columnIndex)
    throws SQLException {
    String columnName = metaData.getColumnName(columnIndex).toLowerCase();
    String columnType = metaData.getColumnTypeName(columnIndex).toLowerCase();
    int columnSize = metaData.getColumnDisplaySize(columnIndex);

    if (columnName.startsWith("is_") || columnType.startsWith("tinyint") && columnSize == 1) {
      return "Boolean";
    } else if ("bit".equals(columnType) && columnSize == 1) {
      return "Boolean";
    } else if ("bit".equals(columnType) && columnSize > 1) {
      return "byte[]";
    }

    return COLUMN_TYPE_2_ATTR_TYPE_MAPPINGS.get(columnType);
  }

  public Connection connection(String url, String username, String password) {
    String driverName = "com.mysql.jdbc.Driver";

    try {
      Class.forName(driverName);
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException(e);
    }

    try {
      return DriverManager.getConnection(url, username, password);
    } catch (SQLException e) {
      return null;
    }
  }

  /**
   * @author yixi
   * @since 1.0.1
   */
  public static class Config {

    private String databaseUrl;
    private String username;
    private String password;
    private List<String> tables;

    public String getDatabaseUrl() {
      return databaseUrl;
    }

    public void setDatabaseUrl(String databaseUrl) {
      this.databaseUrl = databaseUrl;
    }

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }

    public List<String> getTables() {
      return tables;
    }

    public void setTables(List<String> tables) {
      this.tables = tables;
    }
  }

  /**
   * @author yixi
   * @since 1.0.1
   */
  public static class ConfigBuilder {

    private Config config;

    public ConfigBuilder() {
      config = new Config();
    }

    public static ConfigBuilder builder() {
      return new ConfigBuilder();
    }

    public ConfigBuilder databaseUrl(String databaseUrl) {
      config.databaseUrl = databaseUrl;
      return this;
    }

    public ConfigBuilder username(String username) {
      config.username = username;
      return this;
    }

    public ConfigBuilder password(String password) {
      config.password = password;
      return this;
    }

    public ConfigBuilder tables(List<String> tables) {
      config.tables = tables;
      return this;
    }

    public Config build() {
      return config;
    }
  }
}


