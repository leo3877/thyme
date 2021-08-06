package org.yixi.thyme.data.mysql;

import com.alibaba.druid.pool.DruidDataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author yixi
 * @since 1.0.0
 */
public class Test {

  public static void main(String[] args) throws Exception {
    DruidDataSource druidDataSource = new DruidDataSource();
    druidDataSource.setUrl("jdbc:mysql://172.16.24.197:3306");
    druidDataSource.setUsername("yunpian-test");
    druidDataSource.setPassword("A15rtforQpYp");
    PreparedStatement preparedStatement = druidDataSource.getConnection()
      .prepareStatement("select `case_id`, `http_method` from `testdb`.`test_data`");
    boolean execute = preparedStatement.execute();
    ResultSet result = preparedStatement.getResultSet();
    while (result.next()) {
      System.out.println(result.getMetaData());
      Object case_id = result.getObject("case_id");
      System.out.println(case_id.getClass().getSimpleName());
      Object http_method = result.getObject("http_method");
      System.out.println(http_method);
    }
    System.out.println(execute);
    ResultSet resultSet = preparedStatement.executeQuery();
    System.out.println(resultSet.next());
  }

  public static class Op {

  }

  public static class Eq extends Op {

    public static Op eq(String key, Object val) {
      return null;
    }
  }

  public static class And extends Op {

    public static And and(Op... ops) {
      return null;
    }
  }


}
