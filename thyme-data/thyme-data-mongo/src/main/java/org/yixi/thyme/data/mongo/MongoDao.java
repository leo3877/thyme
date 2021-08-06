package org.yixi.thyme.data.mongo;

import com.mongodb.WriteConcern;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import java.util.List;
import java.util.function.Consumer;
import lombok.Data;
import org.bson.Document;
import org.yixi.thyme.data.Constants;
import org.yixi.thyme.data.mongo.DefaultMongoDaoImpl.CursorKeeperResponse;
import org.yixi.thyme.data.mongo.mapper.DocumentMapper;

/**
 * Mongo 数据库库操作接口。
 *
 * @author yixi
 * @since 1.0.0
 */
@SuppressWarnings("all")
public interface MongoDao {

  DocumentMapper getDocumentMapper();

  <PK, T> T get(String database, String collectionName, PK id, Class<T> clazz);

  <T> T findOne(String database, String collectionName, MongoQuery mongoQuery, Class<T> clazz);

  <T> T findUniqueOne(String database, String collectionName, MongoQuery mongoQuery,
    Class<T> clazz);

  <T> List<T> find(String database, String collectionName, MongoQuery mongoQuery, Class<T> clazz);

  <T> void forEach(String database, String collectionName, MongoQuery mongoQuery, Class<T> clazz,
    Consumer<T> consumer);

  <T> CursorKeeperResponse<T> findCursorKeeper(String database, String collectionName,
    MongoQuery mongoQuery, Class<T> clazz);

  <T> CursorKeeperResponse<T> next(Long requestId, String databsase, String collectionName,
    Class<T> clazz);

  /**
   * 获取所有数据，最大能返回 {@code Constants#DEFAULT_MAX_LIMIT} 条数据
   *
   * @see org.yixi.thyme.data.Constants#DEFAULT_MAX_LIMIT
   */
  <T> List<T> findAll(String database, String collectionName, Class<T> clazz);

  FindIterable<Document> findIterable(String database, String collectionName,
    MongoQuery mongoQuery);

  long count(String database, String collectionName, MongoFilter filter);

  <PK, T> PK insert(String database, String collectionName, T doc);

  <T> void insert(String database, String collectionName, List<T> docs);

  <T> void insert(String database, String collectionName, List<T> docs, WriteConcern
    writeConcern);

  <PK, T> PK insert(String database, String collectionName, T doc, WriteConcern writeConcern);

  /**
   * 替换整个文档，而非增量更新。
   *
   * @see MongoDao#update(String, String, Object, Object)
   */
  <PK, T> long replace(String database, String collectionName, PK id, T doc);

  /**
   * 替换整个文档，而非增量更新。
   *
   * @see MongoDao#update(String, String, MongoFilter, MongoUpdate, WriteConcern)
   */
  <PK, T> long replace(String database, String collectionName, PK id, T doc,
    WriteConcern writeConcern);

  // 更新 doc 中不为 null 的字段
  <PK, T> UpdateResult update(String database, String collectionName, PK id, T doc);

  <PK> UpdateResult update(String database, String collectionName, PK id, MongoUpdate update);

  UpdateResult update(String database, String collectionName, MongoFilter filter,
    MongoUpdate update);

  UpdateResult update(String database, String collectionName, MongoFilter filter,
    MongoUpdate update, WriteConcern writeConcern);

  <T> T findOneAndUpdate(String database, String collectionName, MongoQuery filter,
    MongoUpdate update, FindOneAndUpdateOptions options, Class<T> clazz);

  <PK> DeleteResult delete(String database, String collectionName, PK id);

  DeleteResult delete(String database, String collectionName, MongoFilter filter);

  DeleteResult delete(String database, String collectionName, MongoFilter filter,
    WriteConcern writeConcern);

  Document explain(String database, String collectionName, MongoQuery mongoQuery);

  MongoDatabase mongoDatabase(String database);

  /**
   * @author yixi
   * @since 1.1.2
   */
  @Data
  public static class Options {

    private int queryTimeout = Constants.DEFAULT_QUERY_TIMEOUT;

    private int bachSize = Constants.DEFAULT_BATCH_SIZE;
  }
}
