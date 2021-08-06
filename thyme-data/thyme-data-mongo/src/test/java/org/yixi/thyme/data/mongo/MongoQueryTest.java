package org.yixi.thyme.data.mongo;

import com.google.common.collect.Lists;
import org.yixi.thyme.core.json.Jsons;
import org.junit.Test;

/**
 * @author yixi
 * @since 1.0.0
 */
public class MongoQueryTest {

  @Test
  public void test() {
    MongoQuery mongoQuery = MongoQuery.create().key("test").eq("string").key("in_test")
      .in(Lists.newArrayList("test", "test002", "test003"));
    System.out.println(Jsons.encodePretty(mongoQuery.filter().doc()));
  }
}
