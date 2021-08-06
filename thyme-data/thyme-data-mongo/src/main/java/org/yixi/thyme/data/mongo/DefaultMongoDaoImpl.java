package org.yixi.thyme.data.mongo;

import com.mongodb.MongoWriteException;
import com.mongodb.WriteConcern;
import com.mongodb.WriteError;
import com.mongodb.assertions.Assertions;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.InsertManyOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.inject.Singleton;
import lombok.Data;
import org.apache.commons.lang3.time.DateUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yixi.thyme.core.BaseEntity;
import org.yixi.thyme.core.ObjectId;
import org.yixi.thyme.core.Thyme;
import org.yixi.thyme.core.ex.DuplicateKeyException;
import org.yixi.thyme.core.ex.ThymeException;
import org.yixi.thyme.data.Constants;
import org.yixi.thyme.data.mongo.DefaultMongoDaoImpl.MongoLogger.OpType;
import org.yixi.thyme.data.mongo.mapper.CustomDocumentMapper;
import org.yixi.thyme.data.mongo.mapper.DocumentMapper;

/**
 * @author yixi
 * @since 1.0.0
 */
@Singleton
public class DefaultMongoDaoImpl implements MongoDao {

  private static final Logger logger = LoggerFactory.getLogger(DefaultMongoDaoImpl.class);

  private final DocumentMapper documentMapper;

  private final MongoClient mongoClient;
  private final MongoLogger mongoLogger;
  private final Options options;

  public DefaultMongoDaoImpl(MongoClient mongoClient) {
    this(new CustomDocumentMapper(), mongoClient);
  }

  public DefaultMongoDaoImpl(DocumentMapper documentMapper, MongoClient mongoClient) {
    this(documentMapper, mongoClient, new Options());
  }

  public DefaultMongoDaoImpl(DocumentMapper documentMapper, MongoClient mongoClient,
    Options options) {
    this.documentMapper = documentMapper;
    this.mongoClient = mongoClient;
    this.mongoLogger = new MongoLogger();
    this.options = options;
  }

  @Override
  public DocumentMapper getDocumentMapper() {
    return documentMapper;
  }

  @Override
  public <T> List<T> findAll(String database, String collectionName, Class<T> clazz) {
    return find(database, collectionName,
      new MongoQuery().limit(Constants.DEFAULT_MAX_LIMIT), clazz);
  }

  @Override
  public <P, T> P insert(String database, String collectionName, T doc) {
    return insert(database, collectionName, doc, null);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <P, T> P insert(String database, String collectionName, T doc,
    WriteConcern writeConcern) {
    Document document = documentMapper.toDocument(doc);
    document.computeIfAbsent("_id", k -> new ObjectId().toHexString());
    document.computeIfAbsent(BaseEntity.CREATE_TIME, k -> truncate(new Date()));
    document.computeIfAbsent(BaseEntity.UPDATE_TIME, k -> truncate(new Date()));

    mongoLogger.log(MongoLogger.OpType.INSERT, database, collectionName, null, document);

    MongoCollection<Document> collection = mongoClient.getDatabase(database)
      .getCollection(collectionName);
    if (writeConcern != null) {
      collection.withWriteConcern(writeConcern);
    }
    try {
      collection.insertOne(document);
      return (P) document.get("_id");
    } catch (MongoWriteException e) {
      throw checkDuplicateKeyException(e);
    }
  }

  @Override
  public <T> void insert(String database, String collectionName, List<T> docs) {
    insert(database, collectionName, docs, null);
  }

  @Override
  public <T> void insert(String database, String collectionName, List<T> docs,
    WriteConcern writeConcern) {
    List<Document> documents = new ArrayList<>();
    for (T doc : docs) {
      Document document = documentMapper.toDocument(doc);
      documents.add(document);
      document.computeIfAbsent("_id", k -> new ObjectId().toHexString());
      document.computeIfAbsent(BaseEntity.CREATE_TIME, k -> truncate(new Date()));
      document.computeIfAbsent(BaseEntity.UPDATE_TIME, k -> truncate(new Date()));
    }

    mongoLogger.log(MongoLogger.OpType.INSERT, database, collectionName, null, documents);

    MongoCollection<Document> collection = mongoClient.getDatabase(database)
      .getCollection(collectionName);
    if (writeConcern != null) {
      collection.withWriteConcern(writeConcern);
    }
    collection.insertMany(documents, new InsertManyOptions().ordered(false));
  }

  @Override
  public <P, T> long replace(String database, String collectionName, P id, T doc) {
    return replace(database, collectionName, id, doc, null);
  }

  @Override
  public <P, T> long replace(String database, String collectionName, P id, T doc,
    WriteConcern writeConcern) {
    Document document = documentMapper.toDocument(doc);
    document.computeIfAbsent(BaseEntity.UPDATE_TIME, k -> truncate(new Date()));
    Document filter = new Document("_id", id);

    mongoLogger.log(OpType.UPDATE, database, collectionName, filter, document);
    long start = System.currentTimeMillis();
    MongoCollection<Document> collection = mongoClient.getDatabase(database)
      .getCollection(collectionName);
    if (writeConcern != null) {
      collection.withWriteConcern(writeConcern);
    }
    try {
      UpdateResult updateResult = collection.replaceOne(filter, document);
      long spend = System.currentTimeMillis() - start;
      if (spend > 20) {
        logger.warn("[Replace] slow op: {} millis, db: {}, table: {}, filter: {}, update: {}",
          spend, database, collectionName, filter, doc);
      }
      return updateResult.getModifiedCount();
    } catch (MongoWriteException e) {
      throw checkDuplicateKeyException(e);
    }
  }

  @Override
  public <P, T> T get(String database, String collectionName, P id, Class<T> clazz) {
    return findOne(database, collectionName, new MongoQuery().key("_id").eq(id), clazz);
  }

  @Override
  public <T> T findOne(String database, String collectionName, MongoQuery filter,
    Class<T> clazz) {
    filter.limit(1);
    List<T> objects = find(database, collectionName, filter, clazz);
    return objects.isEmpty() ? null : objects.get(0);
  }

  @Override
  public <T> T findUniqueOne(String database, String collectionName, MongoQuery filter,
    Class<T> clazz) {
    filter.limit(2);
    List<T> objects = find(database, collectionName, filter, clazz);
    if (objects.size() >= 2) {
      throw new IllegalArgumentException("Must one. but more than one.");
    }
    return objects.isEmpty() ? null : objects.get(0);
  }

  @Override
  public <T> List<T> find(String database, String collectionName, MongoQuery mongoQuery,
    Class<T> clazz) {
    if (mongoQuery.limit() == 0) {
      mongoQuery.limit(Constants.DEFAULT_LIMIT);
    } else if (mongoQuery.limit() > Constants.DEFAULT_MAX_LIMIT) {
      throw Thyme.ex("Exceeds limit, max limit is %d, but your limit is %d",
        Constants.DEFAULT_MAX_LIMIT, mongoQuery.limit());
    } else if (mongoQuery.limit() < 0) {
      throw Thyme.ex("Limit must be positive number");
    }
    long start = System.currentTimeMillis();
    List<T> docs = new ArrayList<>();
    forEach(database, collectionName, mongoQuery, clazz, doc -> docs.add(doc));
    long spend = System.currentTimeMillis() - start;
    if (spend > 20) {
      logger.warn("[Find] slow op: {} millis, db: {}, table: {}, query: {}",
        spend, database, collectionName, mongoQuery);
    }
    return docs;
  }

  @Override
  public <T> void forEach(String database, String collectionName, MongoQuery mongoQuery,
    Class<T> clazz, Consumer<T> consumer) {
    findIterable(database, collectionName, mongoQuery)
      .forEach((Consumer<Document>) doc -> consumer.accept(documentMapper.toObject(doc, clazz)));
  }

  @Override
  public <T> CursorKeeperResponse<T> findCursorKeeper(String database, String collectionName,
    MongoQuery mongoQuery, Class<T> clazz) {
    mongoLogger.log(MongoLogger.OpType.FIND, database, collectionName, mongoQuery, null);
    Document command = new Document();
    command.put("find", collectionName);
    if (!mongoQuery.filter().doc().isEmpty()) {
      command.put("filter", new Document(mongoQuery.filter().doc()));
    }
    org.yixi.thyme.core.Document<String, Integer> sorts = mongoQuery.sorts();
    if (sorts != null && !sorts.isEmpty()) {
      command.put("sort", new Document((Map) sorts));
    }
    org.yixi.thyme.core.Document<String, Boolean> fields = mongoQuery.fields();
    if (fields != null && !fields.isEmpty()) {
      command.put("projection", new Document((Map) fields));
    }
    if (mongoQuery.skip() > 0) {
      command.put("skip", mongoQuery.skip());
    }
    int limit = mongoQuery.limit();
    if (limit > 0) {
      command.put("limit", limit);
    }
    if (limit > 0 && limit < 1000) {
      command.put("batchSize", limit);
    } else {
      command.put("batchSize", 1000);
    }
    command.put("maxTimeMS", options.getQueryTimeout());
    Document res = mongoClient.getDatabase(database).runCommand(command);
    return parse(false, res, clazz);
  }

  @Override
  public Document explain(String database, String collectionName,
    MongoQuery mongoQuery) {
    mongoLogger.log(MongoLogger.OpType.FIND, database, collectionName, mongoQuery, null);
    Document command = new Document();
    command.put("find", collectionName);
    if (!mongoQuery.filter().doc().isEmpty()) {
      command.put("filter", new Document(mongoQuery.filter().doc()));
    }
    org.yixi.thyme.core.Document<String, Integer> sorts = mongoQuery.sorts();
    if (sorts != null && !sorts.isEmpty()) {
      command.put("sort", new Document((Map) sorts));
    }
    org.yixi.thyme.core.Document<String, Boolean> fields = mongoQuery.fields();
    if (fields != null && !fields.isEmpty()) {
      command.put("projection", new Document((Map) fields));
    }
    if (mongoQuery.skip() > 0) {
      command.put("skip", mongoQuery.skip());
    }
    int limit = mongoQuery.limit();
    if (limit > 0) {
      command.put("limit", limit);
    }
    if (limit > 0 && limit < 1000) {
      command.put("batchSize", limit);
    } else {
      command.put("batchSize", 1000);
    }
    command.put("maxTimeMS", options.getQueryTimeout());
    return mongoClient.getDatabase(database).runCommand(new Document("explain", command));
  }

  @Override
  public <T> CursorKeeperResponse next(Long requestId, String database, String collectionName,
    Class<T> clazz) {
    Document command = new Document();
    command.put("getMore", requestId);
    command.put("collection", collectionName);
    command.put("batchSize", 1000);
//    command.put("maxTimeMS", mongoConfig.getQueryTimeout());
    Document res = mongoClient.getDatabase(database).runCommand(command);
    return parse(true, res, clazz);
  }

  private <T> CursorKeeperResponse<T> parse(boolean next, Document res, Class<T> clazz) {
    Document cursor = (Document) res.get("cursor");
    Long id = (Long) cursor.get("id");
    List<Document> docs =
      next ? (List<Document>) cursor.get("nextBatch") : (List<Document>) cursor.get("firstBatch");
    CursorKeeperResponse<T> keeperResponse = new CursorKeeperResponse<>();
    if (id > 0) {
      keeperResponse.setRequestId(id);
    }
    List newList = new ArrayList();
    for (Document doc : docs) {
      newList.add(documentMapper.toObject(doc, clazz));
    }
    keeperResponse.setDocs(newList);
    return keeperResponse;
  }

  @Override
  public FindIterable<Document> findIterable(String database, String collectionName,
    MongoQuery mongoQuery) {
    mongoLogger.log(MongoLogger.OpType.FIND, database, collectionName, mongoQuery, null);
    MongoCollection<Document> collection = mongoClient.getDatabase(database)
      .getCollection(collectionName);
    FindIterable<Document> findIterable = collection.find(new Document(mongoQuery.filter().doc()));
    org.yixi.thyme.core.Document<String, Integer> sorts = mongoQuery.sorts();
    if (sorts != null && !sorts.isEmpty()) {
      findIterable.sort(new Document((Map) sorts));
    }
    org.yixi.thyme.core.Document<String, Boolean> fields = mongoQuery.fields();
    if (fields != null && !fields.isEmpty()) {
      findIterable.projection(new Document((Map) fields));
    }
    if (mongoQuery.skip() > 0) {
      findIterable.skip(mongoQuery.skip());
    }
    int limit = mongoQuery.limit();
    if (limit > 0) {
      findIterable.limit(limit);
    }
    if (limit > 0 && limit < 1000) {
      findIterable.batchSize(limit);
    } else {
      findIterable.batchSize(options.getBachSize());
    }
    findIterable.maxTime(options.getQueryTimeout(), TimeUnit.MILLISECONDS);
    return findIterable;
  }

  @Override
  public long count(String database, String collectionName, MongoFilter filter) {
    mongoLogger.log(MongoLogger.OpType.COUNT, database, collectionName, filter, null);
    long start = System.currentTimeMillis();
    long count = mongoClient.getDatabase(database).getCollection(collectionName)
      .count(new Document(filter.doc()));
    long spend = System.currentTimeMillis() - start;
    if (spend > 20) {
      logger.warn("[Count] slow op: {} millis, db: {}, table: {}, filter: {}",
        spend, database, collectionName, filter);
    }
    return count;
  }

  @Override
  public <P> UpdateResult update(String database, String collectionName, P id,
    MongoUpdate update) {
    return update(database, collectionName, MongoFilter.create().key("_id").eq(id),
      update);
  }

  @Override
  public <P, T> UpdateResult update(String database, String collectionName, P id, T obj) {
    return update(database, collectionName, new MongoFilter().key("_id").eq(id),
      documentMapper.toMongoUpdate(obj));
  }

  @Override
  public UpdateResult update(String database, String collectionName,
    MongoFilter filter, MongoUpdate mongoUpdate) {
    return update(database, collectionName, filter, mongoUpdate, null);
  }

  @Override
  public UpdateResult update(String database, String collectionName,
    MongoFilter filter,
    MongoUpdate update, WriteConcern writeConcern) {
    Map<String, Map<String, Object>> updateDoc = update.getUpdateDoc();
    if (updateDoc.isEmpty()) {
      return UpdateResult.acknowledged(0L, 0L, null);
    }
    updateDoc.computeIfAbsent("$set", k -> new LinkedHashMap<>());
    Map<String, Object> set = updateDoc.get("$set");
    set.computeIfAbsent(BaseEntity.UPDATE_TIME, k -> truncate(new Date()));
    if (update.isUpsert()) {
      updateDoc.computeIfAbsent("$setOnInsert", k -> new LinkedHashMap<>());
      Map<String, Object> setOnInsert = updateDoc.get("$setOnInsert");
      setOnInsert.computeIfAbsent(BaseEntity.CREATE_TIME, k -> truncate(new Date()));
      if (filter.doc().get("_id") == null) {
        setOnInsert.computeIfAbsent("_id", k -> new ObjectId().toHexString());
      }
    }

    mongoLogger.log(MongoLogger.OpType.UPDATE, database, collectionName, filter, update);

    MongoCollection<Document> collection = mongoClient.getDatabase(database)
      .getCollection(collectionName);
    if (writeConcern != null) {
      collection.withWriteConcern(writeConcern);
    }
    UpdateResult result;
    long start = System.currentTimeMillis();
    try {
      if (update.isUpsert()) {
        result = collection.updateMany(new Document(filter.doc()),
          new Document((Map) updateDoc), new UpdateOptions().upsert(true));
      } else {
        result = collection.updateMany(new Document(filter.doc()),
          new Document((Map) updateDoc));
      }
    } catch (MongoWriteException e) {
      throw checkDuplicateKeyException(e);
    }
    long spend = System.currentTimeMillis() - start;
    if (spend > 20) {
      logger.warn("[Update] slow op: {} millis, db: {}, table: {}, filter: {}, update: {}",
        spend, database, collectionName, filter, update);
    }
    return result;
  }

  @Override
  public <T> T findOneAndUpdate(String database, String collectionName, MongoQuery query,
    MongoUpdate update, FindOneAndUpdateOptions options, Class<T> clazz) {
    Document doc;
    Document updateDoc = new Document((Map) update.getUpdateDoc());
    if (updateDoc.isEmpty()) {
      throw new ThymeException("更新内容不能为空");
    }
    updateDoc.computeIfAbsent("$set", k -> new LinkedHashMap<>());
    Map<String, Object> set = (Map<String, Object>) updateDoc.get("$set");
    set.computeIfAbsent(BaseEntity.UPDATE_TIME, k -> truncate(new Date()));
    if (update.isUpsert() || options != null && options.isUpsert()) {
      updateDoc.computeIfAbsent("$setOnInsert", k -> new LinkedHashMap<>());
      Map<String, Object> setOnInsert = (Map<String, Object>) updateDoc.get("$setOnInsert");
      setOnInsert.computeIfAbsent(BaseEntity.CREATE_TIME, k -> truncate(new Date()));
    }

    long start = System.currentTimeMillis();

    if (options != null) {
      doc = mongoClient.getDatabase(database)
        .getCollection(collectionName)
        .findOneAndUpdate(new Document(query.filter().doc()),
          updateDoc, options);
    } else {
      doc = mongoClient.getDatabase(database)
        .getCollection(collectionName)
        .findOneAndUpdate(new Document(query.filter().doc()),
          updateDoc);
    }

    long spend = System.currentTimeMillis() - start;
    if (spend > 20) {
      logger.warn("[FindOneAndUpdate] slow op: {} millis, db: {}, table: {}, query: {}, update: {}",
        spend, database, collectionName, query, update);
    }

    if (doc != null) {
      return documentMapper.toObject(doc, clazz);
    } else {
      return null;
    }
  }

  @Override
  public <P> DeleteResult delete(String database, String collectionName, P id) {
    return delete(database, collectionName, MongoFilter.create().key("id").eq(id));
  }

  @Override
  public DeleteResult delete(String database, String collectionName,
    MongoFilter filter) {
    return delete(database, collectionName, filter, null);
  }

  @Override
  public DeleteResult delete(String database, String collectionName,
    MongoFilter filter, WriteConcern writeConcern) {
    mongoLogger.log(MongoLogger.OpType.DELETE, database, collectionName, filter, null);
    MongoCollection<Document> collection = mongoClient.getDatabase(database)
      .getCollection(collectionName);
    if (writeConcern != null) {
      collection.withWriteConcern(writeConcern);
    }
    return collection.deleteMany(new Document(filter.doc()));
  }

  @Override
  public MongoDatabase mongoDatabase(String database) {
    return mongoClient.getDatabase(database);
  }

  private RuntimeException checkDuplicateKeyException(MongoWriteException e) {
    // duplicate key error
    if (e.getCode() == 11000) {
      WriteError error = e.getError();
      String message = error.getMessage();
      int i = message.indexOf("dup key:");
      String indexKey = message.substring(message.indexOf("index:") + 7, i - 1);
      if (indexKey.startsWith("_")) {
        indexKey = indexKey.substring(1);
      }
      String[] keys = indexKey.split("_");
      String values = message.substring(i + 8 + 2, message.length() - 2);
      String[] split = values.split(",");
      Map fields = new LinkedHashMap();
      for (int j = 0; j < split.length; j++) {
        String v = split[j];
        fields.put(keys[j * 2], v.substring(2));
      }
      return new DuplicateKeyException(fields);
    }

    return e;
  }

  /**
   * @author yixi
   */
  @Data
  public static class CursorKeeperResponse<T> {

    private Long requestId;
    private List<T> docs;

  }

  private Date truncate(Date date) {
    return DateUtils.truncate(date, Calendar.SECOND);
  }


  /**
   * 输出 Mongo 操作的日志类。
   *
   * @author sneaky
   * @since 1.0.0
   */
  public static class MongoLogger {

    private static final Logger logger = LoggerFactory.getLogger(DefaultMongoDaoImpl.class);

    public MongoLogger() {
    }

    public void log(OpType opType, String database, String collectionName, Object query,
      MongoUpdate update) {
      if (logger.isDebugEnabled()) {
        if (opType == OpType.FIND || opType == OpType.COUNT || opType == OpType.DELETE) {
          Assertions.notNull("query", query);
          logger.debug("[{}] database: {}, collectionName: {}, query: {}",
            opType, database, collectionName, query);
        } else if (opType == OpType.UPDATE) {
          Assertions.notNull("query", query);
          Assertions.notNull("update", update);
          if (logger.isDebugEnabled()) {
            logger.debug("[UPDATE] database: {}, collectionName: {}, query: {}, update: {}",
              database, collectionName, query, update);
          }
        }
      }
    }

    public void log(OpType opType, String database, String collectionName, Document query,
      Object doc) {
      if (logger.isDebugEnabled()) {
        if (opType == OpType.UPDATE) {
          Assertions.notNull("query", query);
          logger.debug("[UPDATE] database:{}, collectionName: {}, query: {}, doc: {}",
            database, collectionName, query, doc);
        } else if (opType == OpType.INSERT) {
          logger.debug("[INSERT] database: {}, collectionName: {}, doc: {} ",
            database, query, doc);
        }
      }
    }

    public enum OpType {

      FIND,

      COUNT,

      INSERT,

      UPDATE,

      DELETE
    }
  }
}
