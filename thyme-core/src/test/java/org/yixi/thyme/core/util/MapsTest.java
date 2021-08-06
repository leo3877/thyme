package org.yixi.thyme.core.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class MapsTest {

  @Test
  public void testNewArrayListMultimap() throws Exception {
    ArrayListMultimap<String, String> result = Maps.newArrayListMultimap();
    result.put("a", "b");
    result.put("a", "c");
    result.put("a", "m");
    Assertions.assertThat(result.asMap()).containsValue(Lists.newArrayList("c", "m", "b"));
  }
}
