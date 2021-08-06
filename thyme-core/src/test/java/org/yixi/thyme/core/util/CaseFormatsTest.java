package org.yixi.thyme.core.util;

import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.yixi.thyme.core.json.Jsons;

public class CaseFormatsTest {

  @Test
  public void testLowerToUnderscore() {
    Map<String, Object> map = new HashMap<>();
    map.put("testObjectHello", "hello");
    map.put("testObjectHello2", "hello");
    map.put("test_hello", "hello");
    CaseFormats.lowerToUnderscore(map);
    Assertions.assertThat(map).hasSize(3).containsKey("test_hello")
      .containsKey("test_object_hello2");
    System.out.println(Jsons.encodePretty(map));
  }

  @Test
  public void testLowerToUnderscore2() {
    Map<String, Object> map = new HashMap<>();
    map.put("testObjectHello", "hello");
    map.put("testObjectHello2", "hello");
    map.put("test_hello", "hello");
    List<Object> objects = Lists.newArrayList(map);
    CaseFormats.lowerToUnderscore(objects);
    HashMap<Object, Object> newMap = new HashMap<>();
    newMap.put("test_object_hello", "hello");
    newMap.put("test_object_hello2", "hello");
    newMap.put("test_hello", "hello");
    Assertions.assertThat(objects).contains(newMap);
  }
}
