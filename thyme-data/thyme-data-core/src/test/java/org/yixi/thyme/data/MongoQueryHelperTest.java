package org.yixi.thyme.data;

import com.google.common.collect.Lists;
import org.yixi.data.client.RestFilter;
import org.yixi.data.client.YixiQuery;
import org.yixi.data.client.YixiQuery.Builder;
import org.yixi.thyme.core.Document;
import org.yixi.thyme.core.json.Jsons;
import org.yixi.thyme.data.graphdata.LocalDataFetcher;
import org.junit.Test;

/**
 * @author yixi
 * @since
 */
public class MongoQueryHelperTest {

  private static final org.yixi.thyme.data.type.TypeConverters converters = new org.yixi.thyme.data.type.TypeConverters(
    LocalDataFetcher.DataSourceType.Mongo);

  @Test
  public void test() {
    YixiQuery yixiQuery = Builder.builder().key("key1").contains("key1")
      .and(RestFilter.create().key("key2").anyOf(Lists.newArrayList("key21", "key22", "key23"))
          .key("created_time").gte(new Document<>("__last_time", "15d")).exists(true),
        RestFilter.create().not().key("key3").startsWith("string2").key("location")
          .nearSphere(100, 100, 50))
      .build();
    System.out.println(Jsons.encodePretty(yixiQuery));
    yixiQuery.validate();

    converters.convert(yixiQuery.getFilter());

    System.out.println(Jsons.encodePretty(yixiQuery));

  }
}
