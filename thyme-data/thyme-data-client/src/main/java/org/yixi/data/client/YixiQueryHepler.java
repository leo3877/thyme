package org.yixi.data.client;

import org.yixi.data.client.YixiQuery.Builder;
import org.yixi.thyme.core.Thyme;
import org.yixi.thyme.core.json.Jsons;
import java.util.List;
import java.util.Map;

/**
 * @author yixi
 * @since 1.0.0
 */
public class YixiQueryHepler {

  public static void main(String[] args) {
    YixiQuery build = Builder.builder().key("a").gt(1).lt(2).key("b").eq(1).build();
    removeField(build, "a");
    System.out.println(Jsons.encodePretty(build));
  }

  /**
   * 删除 Query 某个字段
   */
  public static void removeField(YixiQuery yixiQuery, String key) {
    yixiQuery.getFields().remove(key);
    yixiQuery.getSorts().remove(key);
    remove(yixiQuery.getFilter(), key);
  }

  private static void remove(Map<String, Object> filter, String key0) {
    for (String key : filter.keySet()) {
      if ("__and".equals(key) || "__or".equals(key)) {
        Object o = filter.get(key);
        if (!(o instanceof List)) {
          throw Thyme.ex("syntax error, %s value must be array", key);
        }
        remove((List) o, key0);
      }
    }
    filter.remove(key0);
  }

  private static void remove(List objects, String key0) {
    for (Object obj : objects) {
      if (!(obj instanceof Map)) {
        throw Thyme.ex("syntax error, %s must be Object", obj);
      }
      remove((Map) obj, key0);
    }
  }

}
