package org.yixi.thyme.core;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import org.yixi.thyme.core.json.Jsons;
import java.util.Date;

public class JsonsTest {

  public static void main(String[] args) {
    Father father = Jsons.decode("{\n"
      + "\t\"type\": \"Child1\",\n"
      + "\t\"name\": \"test sub encode and decode.\"\n"
      + "}", Father.class);
    System.out.println((father instanceof Child1));
    System.out.println(Jsons.encodePretty(father));

    Date start = Jsons.decode("2019-08-07 00:00:00", Date.class);
    Date end = Jsons.decode("2019-08-08 00:00:00", Date.class);
    System.out.println(start.getTime());
    System.out.println(end.getTime());

    Document doc = Thyme.toDocument(father);
    System.out.println(Jsons.encodePretty(doc));
  }


  /**
   * @author yixi
   */
  @JsonTypeInfo(visible = true, property = "type", use = Id.NAME)
  @JsonSubTypes({
    @Type(name = "Child1", value = Child1.class)
  })
  public static class Father {

    private String name;

    private boolean bool = true;


    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public void setBool(boolean bool) {
      this.bool = bool;
    }

    public boolean isBool() {
      return bool;
    }
  }

  /**
   * @author yixi
   */
  public static class Child1 extends Father {

    public Child1() {
      super.name = "Child1";
    }

  }

}
