package org.yixi.thyme.core;

import java.util.Date;
import java.util.Map;
import org.junit.Test;
import org.yixi.thyme.core.json.Jsons;
import org.yixi.thyme.core.matcher.Pattern;
import org.yixi.thyme.core.util.Maps;

/**
 * @author yixi
 * @since 1.0.0
 */
public class EntityMapTest {

    @Test
    public void test() {
        EntityMap<Object> entityMap = new EntityMap<>();
        entityMap.setCreateTime(new Date());
        entityMap.setUpdateTime(new Date());
        entityMap.putAll(new Document().append("className", 1).append("b", 2));
        String json = Jsons.encodePretty(entityMap);
        System.out.println(json);
        EntityMap decode = Jsons.decode(json, EntityMap.class);
        System.out.println(Jsons.encodePretty(decode));
    }

    @Test
    public void test001() {
        String dslString = "{\n"
            + "\t\"__not\": [\n"
            + "\t\t{\n"
            + "\t\t\t\"__and\": [\n"
            + "\t\t\t\t{\n"
            + "\t\t\t\t\t\"a\": {\n"
            + "\t\t\t\t\t\t\"gt\": 4\n"
            + "\t\t\t\t\t}\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t{\n"
            + "\t\t\t\t\t\"a\": {\n"
            + "\t\t\t\t\t\t\"gt\": 1\n"
            + "\t\t\t\t\t}\n"
            + "\t\t\t\t}\n"
            + "\t\t\t]\n"
            + "\t\t}\n"
            + "\t]\n"
            + "}";
        Map dsl = Jsons.decode(dslString, Map.class);
        Pattern pattern = Pattern.parse(dsl);
        boolean result = pattern.match(Maps.newSingletonMap("a", 3));
        System.out.println(result);
    }
}
