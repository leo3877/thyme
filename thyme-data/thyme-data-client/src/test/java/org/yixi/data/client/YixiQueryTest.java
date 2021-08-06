package org.yixi.data.client;

import com.google.common.collect.Lists;
import org.yixi.data.client.YixiQuery.Builder;
import org.yixi.thyme.core.Document;
import org.yixi.thyme.core.json.Jsons;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;

/**
 * @author yixi
 * @since 1.0.0
 */
public class YixiQueryTest {

  @Test
  public void test() {
    YixiQuery yixiQuery = Builder.builder().key("key1").contains("key1").key("in")
      .in(Lists.newArrayList("a", "b", "c", "d"))
      .and(RestFilter.create().key("key2").anyOf(Lists.newArrayList("key21", "key22", "key23")),
        RestFilter.create().not().key("key3").startsWith("string2").key("location")
          .nearSphere(100, 100, 50))
      .build();
    System.out.printf(Jsons.encodePretty(yixiQuery));
  }

  @Test
  public void testOp_last_time() {
    TypeConverters converters = new TypeConverters();
    YixiQuery yixiQuery = Builder.builder().key("key1").contains("key1")
      .and(RestFilter.create().key("key2").anyOf(Lists.newArrayList("key21", "key22", "key23"))
          .key("created_time").gte(new Document<>("__last_time", "15d")).exists(true),
        RestFilter.create().not().key("key3").startsWith("string2").key("location")
          .nearSphere(100, 100, 50))
      .build();
    System.out.println(Jsons.encodePretty(yixiQuery));
    yixiQuery.validate();

    yixiQuery.forEachFilter(((container, key, value) -> {
      System.out.println(Jsons.encodePretty(container));
      System.out.println("key: " + key + ", value: " + value);
    }));
  }

  private static final String regex = "^(0|86|17951)?(13[0-9]|15[012356789]|16[0-9]|17[01235678]|18[0-9]|14[56789]|19[15689])[0-9]{8}$";

  public static final Pattern phoneRegex = Pattern.compile(regex);

  public static void main(String[] args) {
    Matcher matcher = phoneRegex.matcher("18682740689");
    Document<String, String> mobile = new Document<>("mobile", "18682740689");

    org.yixi.thyme.core.matcher.Pattern mobiles = org.yixi.thyme.core.matcher.Pattern
      .parse(Builder.builder().key("mobile").regex(regex).build().getFilter());
    boolean match = mobiles.match(mobile);
    System.out.println(match);
  }


}
