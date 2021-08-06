package org.yixi.thyme.data.mongo;

import com.google.common.collect.Lists;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.UpdateResult;
import org.yixi.data.client.RestFilter;
import org.yixi.data.client.YixiQuery;
import org.yixi.data.client.YixiQuery.Builder;
import org.yixi.thyme.core.Document;
import org.yixi.thyme.core.EntityMap;
import org.yixi.thyme.core.json.Jsons;
import org.yixi.thyme.core.json.ThymeJsons;
import org.yixi.thyme.data.mongo.DefaultMongoDaoImpl.CursorKeeperResponse;
import org.yixi.thyme.data.mongo.MongoDao.Options;
import org.yixi.thyme.data.mongo.mapper.CustomDocumentMapper;
import org.yixi.thyme.data.mongo.mapper.DefaultDocumentMapper;
import org.yixi.thyme.data.mongo.tail.MongoOplogTailer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.bson.BsonTimestamp;
import org.junit.Test;

/**
 * @author yixi
 * @since 1.0.0
 */
public class MongoDaoTest {

  private String database = "test";
  private String collectionName = "custom_user";

  private MongoDao mongoDao;
  private MongoClient client;

  {
    MongoClientConfig mongoClientConfig = new MongoClientConfig();
    mongoClientConfig.setUrls("172.16.24.197:27017");
    mongoClientConfig.setPassword("iha0p1ng");
    mongoClientConfig.setAuthDatabase("admin");
    mongoClientConfig.setUsername("weike");
    Options options = new Options();
    options.setBachSize(5000);
    options.setQueryTimeout(2000);

    client = MongoClients.create(mongoClientConfig);
    mongoDao = new DefaultMongoDaoImpl(new DefaultDocumentMapper(), client, options);
  }

  @Test
  public void testBy() {
    org.bson.Document response = client.getDatabase("admin")
      .runCommand(new org.bson.Document("getParameter", "*"));
    System.out.println(Jsons.encodePretty(response));
  }

  @Test
  public void test() {
    List<String> list = Lists.newArrayList("string1", "string2");
    System.out.println(list);
    for (int i = 0; i < 10000; i++) {
      mongoDao.insert(database, collectionName,
        new Document<>().append("key1", "key2").append("key2", 13).append("array", list));
    }
    List<Map> users = mongoDao.find(database, collectionName, MongoQuery.create(), Map.class);
    System.out.println(users);
  }

  @Test
  public void test001() {
    RestFilter restFilter = RestFilter.create().key("array")
      .anyOf(Lists.newArrayList("dd", "s"));
    System.out.println(Jsons.encodePretty(parse(restFilter.filter())));
    List<Map> users = mongoDao
      .find(database, collectionName, MongoQuery.create(parse(restFilter.filter())), Map.class);
    System.out.println(Jsons.encodePretty(users));
  }

  @Test
  public void test002() {
    YixiQuery yixiQuery = Builder.builder().key("key1").contains("key1")
      .and(RestFilter.create().key("key2").anyOf(Lists.newArrayList("key21", "key22", "key23")),
        RestFilter.create().not().key("key3").gt("string2"))
      .build();
    MongoQuery mongoQuery = QueryHelper.from(yixiQuery);
    List<Map> users = mongoDao.find(database, collectionName, MongoQuery.create(), Map.class);
    System.out.println(Jsons.encodePretty(users));
  }

  @Test
  public void test003() {
    CursorKeeperResponse<EntityMap> cursorKeeper = mongoDao
      .findCursorKeeper(database, collectionName, MongoQuery.create(), EntityMap.class);
    long requestId = cursorKeeper.getRequestId();
    System.out.println(cursorKeeper.getDocs().size());
    System.out.println(requestId);
  }

  @Test
  public void test004() {
    CursorKeeperResponse<EntityMap> cursorKeeper = mongoDao
      .next(2219531058152L, database, collectionName, EntityMap.class);
    System.out.println(cursorKeeper.getDocs().size());
  }

  @Test
  public void test005() {
    UpdateResult result = mongoDao
      .update(database, collectionName, MongoFilter.create().key("_id").eq("test0"),
        MongoUpdate.create().set("test", 12).setUpsert(true));
    System.out.println(result.getUpsertedId());
  }

  @Test
  public void test009() {
    MongoCollection<org.bson.Document> collection = mongoDao.mongoDatabase(database)
      .getCollection(collectionName);
    MongoCursor<org.bson.Document> iterator = collection.find(new org.bson.Document())
      .maxTime(5, TimeUnit.SECONDS).iterator();
    while (iterator.hasNext()) {
      System.out.println(iterator.next());
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  @Test
  public void test006() {
//    mongoDao.mongoDatabase(database).getCollection(collectionName)
//      .insertOne(new org.bson.Document("location",
//        new Document("type", "Point")
//          .append("coordinates", Lists.newArrayList(120.062535, 30.309823)))
//        .append("name", "罗家村").append("date", new Date()));
    MongoQuery mongoQuery = MongoQuery.create().key("location")
      .nearSphere(120.110059, 30.289791, 0, 1 * 500).key("name")
      .eq("余杭糖河");

    YixiQuery yixiQuery = Builder.builder().key("location")
      .nearSphere(120.110059, 30.289791, 0, 50000)
      .build();
    mongoQuery = QueryHelper.from(yixiQuery);

    List<EntityMap> entityMaps = mongoDao.find(database, collectionName, mongoQuery,
      EntityMap.class);
//    entityMaps.forEach(item -> {
//      MongoUpdate mongoUpdate = MongoUpdate.create();
//      mongoUpdate.set("location", ((Map) item.get("location")).get("coordinates"));
//      mongoDao.update(database, collectionName, item.getId(), mongoUpdate);
//    });
    Document<String, Object> doc = mongoQuery.filter().doc();
    System.out.println(Jsons.encodePretty(doc));
    System.out.println(Jsons.encodePretty(entityMaps));

    System.out.println("count: " + mongoDao.count(database, collectionName, mongoQuery.filter()));
  }

  @Test
  public void test007() {
    MongoCollection<org.bson.Document> collection = mongoDao.mongoDatabase("market_cloud")
      .getCollection("user_profile");
    org.bson.Document doc = mongoDao
      .explain("market_cloud", "user_profile", MongoQuery.create().key("a").limit(1).exists(false));
    System.out.println(Jsons.encodePretty(doc));
  }

  @Test
  public void test008() {
    YixiQuery yixiQuery = Builder.builder().key("key1").ne("key1").build();
    MongoQuery mongoQuery = QueryHelper.from(yixiQuery);
    MongoCursor<org.bson.Document> mongoCursor = mongoDao
      .findIterable(database, collectionName, mongoQuery).iterator();
    while (mongoCursor.hasNext()) {
      EntityMap entityMap = new CustomDocumentMapper()
        .toObject(mongoCursor.next(), EntityMap.class);
      System.out.println(Jsons.encodePretty(entityMap));
    }
  }

  @Test
  public void test00005() {
    AtomicLong count = new AtomicLong(0);
    mongoDao
      .forEach("yp_sms", "sms", MongoQuery.create().key("a").exists(false), Map.class, obj -> {
        long l = count.addAndGet(1);
        if (l % 10000 == 0) {
          System.out.println(l);
        }
      });
  }

  @Test
  public void test00006() {
    UpdateResult update = mongoDao
      .update("test_001", "tb123", MongoFilter.create().key("a").eq(1232),
        MongoUpdate.create().setUpsert(true).addToSet("array", "array2"));
    System.out.println(Jsons.encodePretty(update));
  }

  @Test
  public void test00007() throws Exception {
    MongoOplogTailer tailer = new MongoOplogTailer(client);
    tailer.setCheckPoint(new BsonTimestamp(0, 0));
    AtomicLong count = new AtomicLong();
    tailer.addOplogListener(oplog -> {
      ThymeJsons.encode(oplog);
      if (count.incrementAndGet() % 1000 == 0) {
        System.out.println(new Date() + ": " + count.get());
      }
    });
    tailer.start();
    Thread.sleep(100000);
  }

  public List<Map> parse(List<Map> list) {
    List newList = new ArrayList();
    list.forEach(item -> newList.add(parse(item)));
    return newList;
  }

  public Map parse(Map map) {
    Map newMap = new HashMap();
    map.forEach((k, v) -> {
      if (k.equals("__and")) {
        newMap.put("$and", parse((List) v));
      } else if (k.equals("__or")) {
        newMap.put("$or", parse((List) v));
      } else if (v instanceof Map) {
        Map m = (Map) v;
        Map valueMap = new HashMap();
        Object not = m.get("__not");
        if (not != null) {
          Map notMap = new HashMap();
          if (not instanceof Map) {
            m = (Map) not;
            notMap.put("$not", valueMap);
          } else {
            m = null;
            notMap.put("$not", not);
          }
          newMap.put(k, notMap);
        } else {
          newMap.put(k, valueMap);
        }
        if (m != null) {
          m.forEach((k1, v1) -> {
            if (k1.equals("eq")) {
              valueMap.put("$eq", v1);
            } else if (k1.equals("ne")) {
              valueMap.put("$ne", v1);
            } else if (k1.equals("gt")) {
              valueMap.put("$gt", v1);
            } else if (k1.equals("gte")) {
              valueMap.put("$gte", v1);
            } else if (k1.equals("lt")) {
              valueMap.put("$lt", v1);
            } else if (k1.equals("lte")) {
              valueMap.put("$lte", v1);
            } else if (k1.equals("in")) {
              valueMap.put("$in", v1);
            } else if (k1.equals("nin")) {
              valueMap.put("$nin", v1);
            } else if (k1.equals("regex")) {
              valueMap.put("$regex", v1);
            } else if (k1.equals("contains")) {
              valueMap.put("$regex", v1);
            } else if (k1.equals("startsWith")) {
              valueMap.put("$regex", "^" + v1);
            } else if (k1.equals("endsWith")) {
              valueMap.put("$regex", v1 + "$");
            } else if (k1.equals("allOf")) {
              valueMap.put("$all", v1);
            } else if (k1.equals("notOf")) {
              Map notOf = new HashMap();
              notOf.put("$all", v1);
              valueMap.put("$not", notOf);
            } else if (k1.equals("anyOf")) {
              Map match = new HashMap();
              match.put("$in", v1);
              valueMap.put("$elemMatch", match);
            } else {
              throw new UnsupportedOperationException("Unsupported Operator: " + k1);
            }
          });
        }
      } else {
        newMap.put(k, v);
      }
    });
    return newMap;
  }

}
