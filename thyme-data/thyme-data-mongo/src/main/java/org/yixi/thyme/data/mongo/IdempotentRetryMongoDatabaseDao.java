package org.yixi.thyme.data.mongo;

import com.mongodb.MongoSocketReadException;
import com.mongodb.MongoWriteException;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.inject.Singleton;
import org.yixi.thyme.core.ObjectId;
import org.yixi.thyme.core.Thyme;
import org.yixi.thyme.core.ex.RetryableException;
import org.yixi.thyme.data.mongo.mapper.DocumentMapper;

/**
 * @author yixi
 * @since 1.0.0
 */
@Singleton
public class IdempotentRetryMongoDatabaseDao implements MongoDatabaseDao {

  private final MongoDatabaseDao mongoDatabaseDao;

  public IdempotentRetryMongoDatabaseDao(MongoDatabaseDao mongoDatabaseDao) {
    this.mongoDatabaseDao = mongoDatabaseDao;
  }

  @Override
  public DocumentMapper getDocumentMapper() {
    return mongoDatabaseDao.getDocumentMapper();
  }

  @Override
  public <PK, T> PK insert(T doc) {
    return retry(() -> mongoDatabaseDao.insert(doc));
  }

  @Override
  public <PK, T> PK insert(T doc, String collectionName) {
    return retry(() -> mongoDatabaseDao.insert(doc, collectionName));
  }

  @Override
  public <PK, T> PK insert(T doc, WriteConcern writeConcern) {
    return retry(() -> mongoDatabaseDao.insert(doc, writeConcern));
  }

  @Override
  public <PK, T> PK insert(T doc, String collectionName, WriteConcern writeConcern) {
    return retry(() -> mongoDatabaseDao.insert(doc, collectionName, writeConcern));
  }

  @Override
  public <T> void insert(List<T> docs) {
    mongoDatabaseDao.insert(docs);
  }

  @Override
  public <T> void insert(List<T> docs, String collectionName) {
    mongoDatabaseDao.insert(docs, collectionName);
  }

  @Override
  public <T> void insert(List<T> docs, WriteConcern writeConcern) {
    mongoDatabaseDao.insert(docs, writeConcern);
  }

  @Override
  public <T> void insert(List<T> docs, String collectionName, WriteConcern writeConcern) {
    List<org.bson.Document> documents = new ArrayList<>();
    for (T doc : docs) {
      org.bson.Document document = mongoDatabaseDao.getDocumentMapper().toDocument(doc);
      document.computeIfAbsent("id", k -> new ObjectId().toHexString());
      documents.add(document);
    }
    mongoDatabaseDao.insert(documents, collectionName, writeConcern);
  }

  @Override
  public <PK, T> long replace(PK id, T doc) {
    return retry(() -> mongoDatabaseDao.replace(id, doc));
  }

  @Override
  public <PK, T> long replace(PK id, T doc, String collectionName) {
    return retry(() -> mongoDatabaseDao.replace(id, doc, collectionName));
  }

  @Override
  public <PK, T> long replace(PK id, T doc, WriteConcern writeConcern) {
    return retry(() -> mongoDatabaseDao.replace(id, doc, writeConcern));
  }

  @Override
  public <PK, T> long replace(PK id, T doc, String collectionName, WriteConcern writeConcern) {
    return retry(() -> mongoDatabaseDao.replace(id, doc, collectionName, writeConcern));
  }

  @Override
  public <PK, T> T get(PK id, Class<T> clazz) {
    return retry(() -> mongoDatabaseDao.get(id, clazz));
  }

  @Override
  public <PK, T> T get(PK id, Class<T> clazz, String collectionName) {
    return retry(() -> mongoDatabaseDao.get(id, clazz, collectionName));
  }

  @Override
  public <T> T findOne(MongoQuery filter, Class<T> clazz) {
    return retry(() -> mongoDatabaseDao.findOne(filter, clazz));
  }

  @Override
  public <T> T findOne(MongoQuery filter, Class<T> clazz, String collectionName) {
    return retry(() -> mongoDatabaseDao.findOne(filter, clazz, collectionName));
  }

  @Override
  public <T> T findUniqueOne(MongoQuery filter, Class<T> clazz) {
    return retry(() -> mongoDatabaseDao.findUniqueOne(filter, clazz));
  }

  @Override
  public <T> T findUniqueOne(MongoQuery filter, Class<T> clazz, String collectionName) {
    return retry(() -> mongoDatabaseDao.findUniqueOne(filter, clazz, collectionName));
  }

  @Override
  public <T> List<T> find(MongoQuery filter, Class<T> clazz) {
    return retry(() -> mongoDatabaseDao.find(filter, clazz));
  }

  @Override
  public <T> List<T> find(MongoQuery filter, Class<T> clazz, String collectionName) {
    return retry(() -> mongoDatabaseDao.find(filter, clazz, collectionName));
  }

  @Override
  public <T> List<T> findAll(Class<T> clazz) {
    return retry(() -> mongoDatabaseDao.findAll(clazz));
  }

  @Override
  public <T> List<T> findAll(Class<T> clazz, String collectionName) {
    return retry(() -> mongoDatabaseDao.findAll(clazz, collectionName));
  }

  @Override
  public <T> void forEach(Class<T> clazz, Consumer<T> consumer) {
    forEach(MongoQuery.create(), clazz, consumer);
  }

  @Override
  public <T> void forEach(MongoQuery mongoQuery, Class<T> clazz, Consumer<T> consumer) {
    retry(() -> {
      mongoDatabaseDao.forEach(mongoQuery, clazz, consumer);
      return null;
    });
  }

  @Override
  public <T> void forEach(String collectionName, MongoQuery mongoQuery, Class<T> clazz,
    Consumer<T> consumer) {
    retry(() -> {
      mongoDatabaseDao.forEach(collectionName, mongoQuery, clazz, consumer);
      return null;
    });
  }

  @Override
  public long count(MongoFilter filter, Class clazz) {
    return retry(() -> mongoDatabaseDao.count(filter, clazz));
  }

  @Override
  public long count(MongoFilter filter, String collectionName) {
    return retry(() -> mongoDatabaseDao.count(filter, collectionName));
  }

  @Override
  public <PK, T> UpdateResult update(PK id, T doc) {
    return retry(() -> mongoDatabaseDao.update(id, doc));
  }

  @Override
  public <PK, T> UpdateResult update(PK id, T doc, String collectionName) {
    return retry(() -> mongoDatabaseDao.update(id, doc, collectionName));
  }

  @Override
  public <PK> UpdateResult update(PK id, MongoUpdate update, Class clazz) {
    return mongoDatabaseDao.update(id, update, clazz);
  }

  @Override
  public <PK> UpdateResult update(PK id, MongoUpdate update, String collectionName) {
    return mongoDatabaseDao.update(id, update, collectionName);
  }

  @Override
  public UpdateResult update(MongoFilter filter, MongoUpdate update, Class clazz) {
    return mongoDatabaseDao.update(filter, update, clazz);
  }

  @Override
  public UpdateResult update(MongoFilter filter, MongoUpdate update, Class clazz,
    WriteConcern writeConcern) {
    return mongoDatabaseDao.update(filter, update, clazz, writeConcern);
  }

  @Override
  public UpdateResult update(MongoFilter filter, MongoUpdate update,
    String collectionName) {
    return mongoDatabaseDao.update(filter, update, collectionName);
  }

  @Override
  public UpdateResult update(MongoFilter filter, MongoUpdate update,
    String collectionName,
    WriteConcern writeConcern) {
    return mongoDatabaseDao.update(filter, update, collectionName, writeConcern);
  }

  @Override
  public <T> T findOneAndUpdate(MongoQuery filter, MongoUpdate update,
    FindOneAndUpdateOptions options, Class<T> clazz) {
    return mongoDatabaseDao.findOneAndUpdate(filter, update, options, clazz);
  }

  @Override
  public <PK> DeleteResult delete(PK id, Class clazz) {
    return retry(() -> mongoDatabaseDao.delete(id, clazz));
  }

  @Override
  public <PK> DeleteResult delete(PK id, String collectionName) {
    return retry(() -> mongoDatabaseDao.delete(id, collectionName));
  }

  @Override
  public DeleteResult delete(MongoFilter filter, Class clazz) {
    return retry(() -> mongoDatabaseDao.delete(filter, clazz));
  }

  @Override
  public DeleteResult delete(MongoFilter filter, Class clazz,
    WriteConcern writeConcern) {
    return retry(() -> mongoDatabaseDao.delete(filter, clazz, writeConcern));
  }

  @Override
  public DeleteResult delete(MongoFilter filter, String collectionName) {
    return retry(() -> mongoDatabaseDao.delete(filter, collectionName));
  }

  @Override
  public DeleteResult delete(MongoFilter filter, String collectionName,
    WriteConcern writeConcern) {
    return null;
  }

  @Override
  public MongoDatabase mongoDatabase() {
    return mongoDatabaseDao.mongoDatabase();
  }

  /**
   * 进行更多的重试异常测试
   */
  private <R> R retry(Supplier<R> func) {
    return (R) Thyme.retry(2, () -> {
      try {
        return func.get();
      } catch (MongoWriteException e) {
        if (e.getError().getCode() == 11000
          && e.getError().getMessage().indexOf("index: _id_ dup key")
          > 0) { // 已经创建成功, 直接返回 id
          String message = e.getError().getMessage();
          // E11000 duplicate key error collection: finance_user.test index: _id_ dup
          // key: { : "593770cfd3bc564a40653fcc" }
          return message.substring(message.indexOf(": \"") + 3, message.length() - 3);
        } else {
          throw e;
        }
      } catch (MongoSocketReadException e) { // 发生读取异常会进行重试
        return new RetryableException(e);
      }
    });
  }
}
