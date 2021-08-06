package org.yixi.thyme.data.mongo;

import org.junit.Test;
import org.yixi.data.client.YixiQuery;
import org.yixi.thyme.core.Platform;
import org.yixi.thyme.core.json.Jsons;
import org.yixi.thyme.data.graphdata.LocalDataFetcher;
import org.yixi.thyme.data.type.TypeConverters;

/**
 * @author yixi
 * @since
 */
public class QueryHelperTest {

  @Test
  public void test() {
//    YixiQuery yixiQuery = Builder.builder().key("key1").contains("").key("key2").gt(null).key("key1006")
//      .and(RestFilter.create().key("key2").anyOf(Lists.newArrayList("key21", "key22", "key23")),
//        RestFilter.create().not().key("key3").startsWith("string2"))
//      .build();
//    System.out.println(Jsons.encodePretty(yixiQuery.getFilter()));
//    MongoQuery mongoQuery = QueryHelper.from(yixiQuery);
//    System.out.println(Jsons.encodePretty(mongoQuery.filter().doc()));

    YixiQuery map = Jsons.decode(
      "{\"count\":true,\"filter\":{\"94068322074625_gmt_updated\":{\"gt\":{\"__date\":\"2020-07-01 14:19:00\"},\"lt\":{\"__date\":\"2020-07-05 14:19:00\"}},\"94068322074625_last_live_time\":{\"gt\":{\"__date\":\"\"},\"lt\":{\"__date\":\"\"}}},\"limit\":20,\"skip\":0}\"",
      YixiQuery.class);
    MongoQuery from = QueryHelper.from(map);
    System.out.println(Jsons.encodePretty(from.filter().doc()));
  }

  @Test
  public void test001() {
    TypeConverters converters = new TypeConverters(LocalDataFetcher.DataSourceType.Mongo);
    String dslString = "{\"count\":true,\"fields\":{},\"filter\":{\"channel_id\":93904788127745,\"biz_date\":{\"__date\":\"2018-13-13 21:33:33\"}},\"limit\":0,\"skip\":0,\"sorts\":{}}";
    YixiQuery yixiQuery = Jsons.decode(dslString, YixiQuery.class);
    System.out.println(Jsons.encodePretty(QueryHelper.toMongoFilter(yixiQuery.getFilter())));
  }

  @Test
  public void test002() throws Exception {
    System.out.println(Platform.get().ip());
  }
}
