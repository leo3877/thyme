package org.yixi.thyme.data;

import org.yixi.data.client.TypeConverters;
import org.yixi.data.client.YixiQuery;
import org.yixi.data.client.YixiQuery.Builder;
import org.yixi.thyme.core.Document;
import org.yixi.thyme.core.json.Jsons;
import java.util.Date;
import org.junit.Test;

/**
 * @author yixi
 * @since 1.0.0
 */
public class TypeConvertersTest {

  @Test
  public void test() {
    TypeConverters typeConverters = new TypeConverters();
    System.out.println(typeConverters.convert(new Date()));

    Document doc = new Document("date", new Date())
      .append("child", new Document("date1", new Date()));
    System.out.println(typeConverters.convert(doc));
    System.out.println(Jsons.encodePretty(doc));

    YixiQuery yixiQuery = Builder.builder().key("date").eq(new Date()).build();
    System.out.println(Jsons.encodePretty(yixiQuery));


  }
}
