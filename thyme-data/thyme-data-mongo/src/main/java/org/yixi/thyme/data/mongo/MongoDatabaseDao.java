package org.yixi.thyme.data.mongo;

import com.mongodb.WriteConcern;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import java.util.List;
import java.util.function.Consumer;
import org.yixi.thyme.data.mongo.mapper.DocumentMapper;

/**
 * @author yixi
 * @since 1.0.0
 */
@SuppressWarnings("all")
public interface MongoDatabaseDao {

  DocumentMapper getDocumentMapper();

  <PK, T> PK insert(T doc);

  <PK, T> PK insert(T doc, String collectionName);

  /**
   * 添加文档（collectionName 根据类型自动生成）。
   */
  <PK, T> PK insert(T doc, WriteConcern writeConcern);

  /**
   * 添加文档。
   */
  <PK, T> PK insert(T doc, String collectionName, WriteConcern writeConcern);

  /**
   * 添加文档（collectionName 根据类型自动生成）。
   */
  <T> void insert(List<T> docs);

  /**
   * 添加文档。
   */
  <T> void insert(List<T> docs, String collectionName);

  /**
   * 添加文档（collectionName 根据类型自动生成）。
   */
  <T> void insert(List<T> docs, WriteConcern writeConcern);

  /**
   * 添加文档。
   */
  <T> void insert(List<T> docs, String collectionName, WriteConcern writeConcern);

  /**
   * 根据 {@code id} 全量替换整个文档，而非增量更新。
   *
   * @see MongoDatabaseDao#update(Object, Object)
   * @see MongoDatabaseDao#update(MongoFilter, MongoUpdate, Class)
   */
  <PK, T> long replace(PK id, T doc);

  /**
   * 根据 {@code id} 全量替换整个文档，而非增量更新。
   *
   * @see MongoDatabaseDao#update(Object, MongoUpdate, String)
   * @see MongoDatabaseDao#update(MongoFilter, MongoUpdate, String, WriteConcern)
   */
  <PK, T> long replace(PK id, T doc, String collectionName);

  /**
   * 根据 {@code id} 全量替换整个文档，而非增量更新。
   *
   * @see MongoDatabaseDao#update(Object, MongoUpdate, Class)
   * @see MongoDatabaseDao#update(MongoFilter, MongoUpdate, Class, WriteConcern)
   */
  <PK, T> long replace(PK id, T doc, WriteConcern writeConcern);

  /**
   * 根据 {@code id} 全量替换整个文档，而非增量更新。
   *
   * @see MongoDatabaseDao#update(Object, MongoUpdate, String)
   * @see MongoDatabaseDao#update(MongoFilter, MongoUpdate, String, WriteConcern)
   */
  <PK, T> long replace(PK id, T doc, String collectionName, WriteConcern writeConcern);

  <PK, T> T get(PK id, Class<T> clazz);

  <PK, T> T get(PK id, Class<T> clazz, String collectionName);

  <T> T findOne(MongoQuery filter, Class<T> clazz);

  <T> T findOne(MongoQuery filter, Class<T> clazz, String collectionName);

  <T> T findUniqueOne(MongoQuery filter, Class<T> clazz);

  <T> T findUniqueOne(MongoQuery filter, Class<T> clazz, String collectionName);

  <T> List<T> find(MongoQuery filter, Class<T> clazz);

  <T> List<T> find(MongoQuery filter, Class<T> clazz, String collectionName);

  <T> List<T> findAll(Class<T> clazz);

  <T> List<T> findAll(Class<T> clazz, String collectionName);

  <T> void forEach(Class<T> clazz, Consumer<T> consumer);

  <T> void forEach(MongoQuery mongoQuery, Class<T> clazz, Consumer<T> consumer);

  <T> void forEach(String collectionName, MongoQuery mongoQuery, Class<T> clazz,
    Consumer<T> consumer);

  long count(MongoFilter filter, Class clazz);

  long count(MongoFilter filter, String collectionName);

  /**
   * 根据 {@code id} 更新 {@code doc} 中不为 {@code null} 的字段。
   */
  <PK, T> UpdateResult update(PK id, T doc);

  /**
   * 根据 {@code id} 更新 {@code doc} 中不为 {@code null} 的字段。
   */
  <PK, T> UpdateResult update(PK id, T doc, String collectionName);

  /**
   * 根据 {@code id} 以 {@code update} 对文档进行更新。
   */
  <PK> UpdateResult update(PK id, MongoUpdate update, Class clazz);

  /**
   * 根据 {@code id} 以 {@code update} 对文档进行更新。
   */
  <PK> UpdateResult update(PK id, MongoUpdate update, String collectionName);

  /**
   * 根据 {@code filter} 以 {@code update} 对文档进行更新。
   */
  UpdateResult update(MongoFilter filter, MongoUpdate update, Class clazz);

  /**
   * 根据 {@code filter} 以 {@code update} 对文档进行更新。
   */
  UpdateResult update(MongoFilter filter, MongoUpdate update, Class clazz,
    WriteConcern writeConcern);

  /**
   * 根据 {@code filter} 以 {@code update} 对文档进行更新。
   */
  UpdateResult update(MongoFilter filter, MongoUpdate update, String collectionName);

  /**
   * 根据 {@code filter} 以 {@code update} 对文档进行更新。
   */
  UpdateResult update(MongoFilter filter, MongoUpdate update, String collectionName,
    WriteConcern writeConcern);

  /**
   * 根据 {@code filter} 以 {@code update} 对文档进行更新并返回查询结果。
   */
  <T> T findOneAndUpdate(MongoQuery query, MongoUpdate update, FindOneAndUpdateOptions options,
    Class<T> clazz);

  /**
   * 根据 {@code id} 删除匹配的文档。
   */
  <PK> DeleteResult delete(PK id, Class clazz);

  /**
   * 根据 {@code id} 删除匹配的文档。
   */
  <PK> DeleteResult delete(PK id, String collectionName);

  /**
   * 根据 {@code filter} 删除匹配的文档。
   */
  DeleteResult delete(MongoFilter filter, Class clazz);

  /**
   * 根据 {@code filter} 删除匹配的文档。
   */
  DeleteResult delete(MongoFilter filter, Class clazz, WriteConcern writeConcern);

  /**
   * 根据 {@code filter} 删除匹配的文档。
   */
  DeleteResult delete(MongoFilter filter, String collectionName);

  /**
   * 根据 {@code filter} 删除匹配的文档。
   */
  DeleteResult delete(MongoFilter filter, String collectionName,
    WriteConcern writeConcern);

  MongoDatabase mongoDatabase();

}
