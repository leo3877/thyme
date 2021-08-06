package org.yixi.thyme.data.mongo;

import com.mongodb.MongoSocketReadException;
import com.mongodb.MongoWriteException;
import com.mongodb.WriteConcern;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.inject.Singleton;
import org.bson.Document;
import org.yixi.thyme.core.Thyme;
import org.yixi.thyme.core.ex.RetryableException;
import org.yixi.thyme.data.mongo.DefaultMongoDaoImpl.CursorKeeperResponse;
import org.yixi.thyme.data.mongo.mapper.DocumentMapper;

/**
 * @author yixi
 * @since 1.0.0
 */
@Singleton
public class IdempotentRetryMongoDao implements MongoDao {

  private final MongoDao mongoDao;

  public IdempotentRetryMongoDao(MongoDao mongoDao) {
    this.mongoDao = mongoDao;
  }

  @Override
  public DocumentMapper getDocumentMapper() {
    return mongoDao.getDocumentMapper();
  }

  @Override
  public <PK, T> T get(String database, String collectionName, PK id, Class<T> clazz) {
    return retry(() -> mongoDao.get(database, collectionName, id, clazz));
  }

  @Override
  public <T> T findOne(String database, String collectionName, MongoQuery filter, Class<T> clazz) {
    return retry(() -> mongoDao.findOne(database, collectionName, filter, clazz));
  }

  @Override
  public <T> T findUniqueOne(String database, String collectionName, MongoQuery filter,
    Class<T> clazz) {
    return retry(() -> mongoDao.findUniqueOne(database, collectionName, filter, clazz));
  }

  @Override
  public <T> List<T> find(String database, String collectionName, MongoQuery mongoQuery,
    Class<T> clazz) {
    return retry(() -> mongoDao.find(database, collectionName, mongoQuery, clazz));
  }

  @Override
  public <T> void forEach(String database, String collectionName, MongoQuery mongoQuery,
    Class<T> clazz, Consumer<T> consumer) {
    mongoDao.forEach(database, collectionName, mongoQuery, clazz, consumer);
  }

  @Override
  public <T> CursorKeeperResponse<T> findCursorKeeper(String database, String collectionName,
    MongoQuery mongoQuery, Class<T> clazz) {
    return retry(() -> mongoDao.findCursorKeeper(database, collectionName, mongoQuery, clazz));
  }

  @Override
  public <T> CursorKeeperResponse next(Long requestId, String databsase, String collectionName,
    Class<T> clazz) {
    return mongoDao.next(requestId, databsase, collectionName, clazz);
  }

  @Override
  public FindIterable<Document> findIterable(String database, String collectionName,
    MongoQuery mongoQuery) {
    return mongoDao.findIterable(database, collectionName, mongoQuery);
  }

  @Override
  public <T> List<T> findAll(String database, String collectionName, Class<T> clazz) {
    return retry(() -> mongoDao.findAll(database, collectionName, clazz));
  }

  @Override
  public long count(String database, String collectionName, MongoFilter filter) {
    return retry(() -> mongoDao.count(database, collectionName, filter));
  }

  @Override
  public <PK, T> PK insert(String database, String collectionName, T doc) {
    return retry(() -> mongoDao.insert(database, collectionName, doc));
  }

  @Override
  public <T> void insert(String database, String collectionName, List<T> docs) {
    mongoDao.insert(database, collectionName, docs);
  }

  @Override
  public <T> void insert(String database, String collectionName, List<T> docs,
    WriteConcern writeConcern) {
    mongoDao.insert(database, collectionName, docs, writeConcern);
  }

  @Override
  public <PK, T> PK insert(String database, String collectionName, T doc,
    WriteConcern writeConcern) {
    return retry(() -> mongoDao.insert(database, collectionName, doc, writeConcern));
  }

  @Override
  public <PK, T> long replace(String database, String collectionName, PK id, T doc) {
    return retry(() -> mongoDao.replace(database, collectionName, id, doc));
  }

  @Override
  public <PK, T> long replace(String database, String collectionName, PK id, T doc,
    WriteConcern writeConcern) {
    return retry(() -> mongoDao.replace(database, collectionName, id, doc, writeConcern));
  }

  @Override
  public <PK, T> UpdateResult update(String database, String collectionName, PK id, T doc) {
    return retry(() -> mongoDao.update(database, collectionName, id, doc));
  }

  @Override
  public <PK> UpdateResult update(String database, String collectionName, PK id,
    MongoUpdate update) {
    return mongoDao.update(database, collectionName, id, update);
  }

  @Override
  public UpdateResult update(String database, String collectionName, MongoFilter filter,
    MongoUpdate update) {
    return mongoDao.update(database, collectionName, filter, update);
  }

  @Override
  public UpdateResult update(String database, String collectionName, MongoFilter filter,
    MongoUpdate update, WriteConcern writeConcern) {
    return mongoDao.update(database, collectionName, filter, update, writeConcern);
  }

  @Override
  public <T> T findOneAndUpdate(String database, String collectionName, MongoQuery filter,
    MongoUpdate update, FindOneAndUpdateOptions options, Class<T> clazz) {
    return retry(
      () -> mongoDao.findOneAndUpdate(database, collectionName, filter, update, options, clazz));
  }

  @Override
  public <PK> DeleteResult delete(String database, String collectionName, PK id) {
    return retry(() -> mongoDao.delete(database, collectionName, id));
  }

  @Override
  public DeleteResult delete(String database, String collectionName, MongoFilter filter) {
    return retry(() -> mongoDao.delete(database, collectionName, filter));
  }

  @Override
  public DeleteResult delete(String database, String collectionName, MongoFilter filter,
    WriteConcern writeConcern) {
    return retry(() -> mongoDao.delete(database, collectionName, filter, writeConcern));
  }

  @Override
  public Document explain(String database, String collectionName, MongoQuery mongoQuery) {
    return retry(() -> mongoDao.explain(database, collectionName, mongoQuery));
  }

  @Override
  public MongoDatabase mongoDatabase(String database) {
    return mongoDao.mongoDatabase(database);
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
          && e.getError().getMessage().indexOf("index: _id_ dup key") > 0) { // 已经创建成功, 直接返回 id
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
