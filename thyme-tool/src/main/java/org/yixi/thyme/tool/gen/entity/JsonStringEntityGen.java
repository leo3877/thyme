package org.yixi.thyme.tool.gen.entity;

import org.yixi.thyme.core.ex.ThymeException;
import org.yixi.thyme.core.json.Jsons;
import org.yixi.thyme.tool.gen.entity.EntityGen.ClassField.ClassFieldBuilder;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yixi
 * @since 1.0.0
 */
@SuppressWarnings("all")
public class JsonStringEntityGen extends AbstractEntityGen {

  private static final Logger logger = LoggerFactory.getLogger(JsonStringEntityGen.class);

  public JsonStringEntityGen(String packageName, String basedir, String author) {
    super(packageName, basedir, author);
  }

  public void gen(String jsonString, String className, Profile profile) {
    gen(jsonString, null, className, profile);
  }

  public void gen(String jsonString, String match, String className, Profile profile) {
    Map target = null;
    boolean isArray = false;
    for (char c : jsonString.toCharArray()) {
      if (c == ' ' || c == '\n') {
        continue;
      }
      if (c == '[') {
        isArray = true;
      }
      break;
    }
    if (!isArray) {
      target = Jsons.decode(jsonString, Map.class);
    } else {
      List<Map> l = Jsons.decodeList(jsonString, Map.class);
      if (l.size() > 0) {
        target = l.get(0);
      }
    }
    gen(target, match, className, profile);
  }

  /**
   * 通过 Map 对象生成具体的实体模型对象
   */
  public void gen(Map source, String className, Profile profile) {
    gen(source, null, className, profile);
  }

  /**
   * 通过 Map 对象生成具体的实体模型对象
   */
  public void gen(Map source, String match, String className, Profile profile) {
    Map<String, Object> target = source;
    if ((match != null)) {
      String[] keys = match.split("\\.");
      for (String key : keys) {
        if (target != null) {
          Object o = target.get(key);
          if (o instanceof Map) {
            target = (Map) o;
          } else if (o instanceof List) {
            List l = (List) o;
            if (l.size() > 0 && l.get(0) instanceof Map) {
              target = (Map) l.get(0);
            } else {
              target = null;
            }
          } else {
            target = null;
          }
        } else {
          break;
        }
      }
    }
    if (target == null) {
      throw new ThymeException("无法解析 Response: " + Jsons.encodePretty(source));
    }

    EntitySchema entitySchema = new EntitySchema(className);
    target.forEach((k, v) -> {
      ClassFieldBuilder classFieldBuilder = ClassField.builder().fieldName(fieldName(k))
        .columnName(camelToLower(k));
      if (v == null) {
        classFieldBuilder.type("String"); // 属性为为空则默认设置 String 类型
      } else if (Map.class.isAssignableFrom(v.getClass())) {
        classFieldBuilder.type("Map");
      } else if (List.class.isAssignableFrom(v.getClass())) {
        classFieldBuilder.type("List");
      } else {
        classFieldBuilder.type(v.getClass().getSimpleName());
      }
      entitySchema.addFiled(classFieldBuilder.build());
    });

    gen(entitySchema, profile);
  }
}


