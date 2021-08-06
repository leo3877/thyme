package org.yixi.thyme.data.mongo.mapper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.bson.Document;
import org.junit.Assert;
import org.junit.Test;
import org.yixi.thyme.core.ObjectId;
import org.yixi.thyme.core.json.Jsons;

/**
 * @author yixi
 * @since 1.0.0
 */
public class DefaultDocumentMapperTest {

  private DocumentMapper documentMapper = new DefaultDocumentMapper();

  @Test
  public void testToDocument() {
    Map map = new HashMap();
    map.put("long", 1L);
    map.put("string", "string");
    map.put("int", 1);
    map.put("float", 11.2f);
    map.put("double", 131.2d);
    map.put("date", new Date());
    map.put("enum", TestType.A);

    Document document = documentMapper.toDocument(map);
    Assert.assertEquals(7, document.size());
    Assert.assertEquals(Integer.class, document.get("int").getClass());
    Assert.assertEquals(Float.class, document.get("float").getClass());
    Assert.assertEquals(Double.class, document.get("double").getClass());
    Assert.assertEquals(String.class, document.get("string").getClass());
    Assert.assertEquals(Long.class, document.get("long").getClass());
    Assert.assertEquals(Date.class, document.get("date").getClass());
    Assert.assertEquals(String.class, document.get("enum").getClass());

    A a = A.builder().i(1).f(11.2f).d(11.2).l(11).date(new Date()).testType(TestType.B)
      .string("string").b(B.builder().string("string").build()).build();
    a.setId(new ObjectId().toHexString());
    a.setCreateTime(new Date());
    a.setUpdateTime(new Date());
    document = documentMapper.toDocument(a);

    Assert.assertEquals(11, document.size());
    Assert.assertEquals(Integer.class, document.get("i").getClass());
    Assert.assertEquals(Double.class, document.get("d").getClass());
    Assert.assertEquals(Float.class, document.get("f").getClass());
    Assert.assertEquals(String.class, document.get("string").getClass());
    Assert.assertEquals(Long.class, document.get("l").getClass());
    Assert.assertEquals(Date.class, document.get("date").getClass());
    Assert.assertEquals(String.class, document.get("test_type").getClass());

    System.out.println(Jsons.encodePretty(document));

  }

  @Test
  public void testToObjects() {
    String json = "{\n"
      + "  \"date\" : \"2019-06-16 11:34:47\",\n"
      + "  \"b\" : {\n"
      + "    \"d\" : 1,\n"
      + "    \"string\" : \"string\",\n"
      + "    \"f\" : 1,\n"
      + "    \"i\" : 1,\n"
      + "    \"l\" : 2\n"
      + "  },\n"
      + "  \"d\" : 11.2,\n"
      + "  \"string\" : \"string\",\n"
      + "  \"f\" : 11.2,\n"
      + "  \"i\" : 1,\n"
      + "  \"test_type\" : \"B\",\n"
      + "  \"id\" : \"5d05b8d7cdd5181f1619c3e8\",\n"
      + "  \"l\" : 11\n"
      + "}";
    Document document = Jsons.decode(json, Document.class);
    document.put("b", new Document((Map<String, Object>) document.get("b")));
    Assertions.assertThatThrownBy(() -> documentMapper.toObject(document, A.class))
      .as("测试 type mismatch 场景").isInstanceOf(IllegalArgumentException.class)
      .hasMessageStartingWith(
        "type mismatch: target type: java.util.Date, actual type: java.lang.String, key: date, content:");
    document.put("date", Jsons.decode("2019-06-16 11:34:47", Date.class));
    A a = documentMapper.toObject(document, A.class);
    System.out.println(Jsons.encodePretty(a));
    Assertions.assertThat(Jsons.toJsonNode(a))
      .isEqualTo(Jsons.toJsonNode(Jsons.decode(json, A.class)));
    System.out.println(Jsons.encodePretty(a));

    Map decode = Jsons.decode(json, Map.class);
    System.out.println(true);
  }

  @Test
  public void testToObjects2() {
    Document doc = new Document("class_name", new Document("test_name", new Document("test_n", 1)));
    B a = documentMapper.toObject(doc, B.class);
    System.out.println(Jsons.encodePretty(a));
  }


  public static class T {

    private double f;
    private long l;

    public double getF() {
      return f;
    }

    public void setF(double f) {
      this.f = f;
    }


    public long getL() {
      return l;
    }

    public void setL(long l) {
      this.l = l;
    }
  }

  enum TestType {

    A,

    B
  }


}
