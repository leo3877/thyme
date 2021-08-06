package org.yixi.thyme.data.mongo;

import com.google.common.base.CaseFormat;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import javax.inject.Singleton;
import javax.persistence.Table;
import org.yixi.thyme.core.ex.ThymeException;
import org.yixi.thyme.data.mongo.mapper.DocumentMapper;

/**
 * @author yixi
 * @since 1.0.0
 */
@Singleton
public class DefaultMongoDatabaseDao implements MongoDatabaseDao {

  private final ConcurrentMap<Class, String> collectionNames = new ConcurrentHashMap<>();

  private final MongoDao mongoDao;
  private final String database;

  public DefaultMongoDatabaseDao(MongoDao mongoDao,
    String database) {
    this.database = database;
    this.mongoDao = mongoDao;
  }

  @Override
  public DocumentMapper getDocumentMapper() {
    return mongoDao.getDocumentMapper();
  }

  @Override
  public <P, T> P insert(T doc) {
    return insert(doc, collectionName(doc.getClass()));
  }

  @Override
  public <P, T> P insert(T doc, String collectionName) {
    return insert(doc, collectionName, null);
  }

  @Override
  public <P, T> P insert(T doc, WriteConcern writeConcern) {
    return insert(doc, collectionName(doc.getClass()), writeConcern);
  }

  @Override
  public <P, T> P insert(T doc, String collectionName, WriteConcern writeConcern) {
    return mongoDao.insert(database, collectionName, doc, writeConcern);
  }

  @Override
  public <T> void insert(List<T> docs) {
    insert(docs, (WriteConcern) null);
  }

  @Override
  public <T> void insert(List<T> docs, String collectionName) {
    if (docs != null && docs.size() > 0) {
      insert(docs, collectionName, null);
    } else {
      throw new ThymeException("docs 不能为空");
    }
  }

  @Override
  public <T> void insert(List<T> docs, WriteConcern writeConcern) {
    if (docs != null && docs.size() > 0) {
      insert(docs, collectionName(docs.get(0).getClass()), writeConcern);
    } else {
      throw new ThymeException("docs 不能为空");
    }
  }

  @Override
  public <T> void insert(List<T> docs, String collectionName, WriteConcern writeConcern) {
    mongoDao.insert(database, collectionName, docs, writeConcern);
  }

  @Override
  public <P, T> long replace(P id, T doc) {
    return replace(id, doc, (WriteConcern) null);
  }

  @Override
  public <PK, T> long replace(PK id, T doc, String collectionName) {
    return replace(id, doc, collectionName, null);
  }

  @Override
  public <PK, T> long replace(PK id, T doc, WriteConcern writeConcern) {
    return replace(id, doc, collectionName(doc.getClass()), writeConcern);
  }

  @Override
  public <P, T> long replace(P id, T doc, String collectionName, WriteConcern writeConcern) {
    return mongoDao.replace(database, collectionName, id, doc, writeConcern);
  }

  @Override
  public <P, T> T get(P id, Class<T> clazz) {
    return get(id, clazz, collectionName(clazz));
  }

  @Override
  public <PK, T> T get(PK id, Class<T> clazz, String collectionName) {
    return mongoDao.get(database, collectionName, id, clazz);
  }

  @Override
  public <T> T findOne(MongoQuery filter, Class<T> clazz) {
    return findOne(filter, clazz, collectionName(clazz));
  }

  @Override
  public <T> T findOne(MongoQuery filter, Class<T> clazz, String collectionName) {
    return mongoDao.findOne(database, collectionName, filter, clazz);
  }

  @Override
  public <T> T findUniqueOne(MongoQuery filter, Class<T> clazz) {
    return findUniqueOne(filter, clazz, collectionName(clazz));
  }

  @Override
  public <T> T findUniqueOne(MongoQuery filter, Class<T> clazz, String collectionName) {
    return mongoDao.findUniqueOne(database, collectionName, filter, clazz);
  }

  @Override
  public <T> List<T> find(MongoQuery filter, Class<T> clazz) {
    return find(filter, clazz, collectionName(clazz));
  }

  @Override
  public <T> List<T> find(MongoQuery filter, Class<T> clazz, String collectionName) {
    return mongoDao.find(database, collectionName, filter, clazz);
  }

  @Override
  public <T> List<T> findAll(Class<T> clazz) {
    return findAll(clazz, collectionName(clazz));
  }

  @Override
  public <T> List<T> findAll(Class<T> clazz, String collectionName) {
    return mongoDao.findAll(database, collectionName, clazz);
  }

  @Override
  public <T> void forEach(Class<T> clazz, Consumer<T> consumer) {
    forEach(MongoQuery.create(), clazz, consumer);
  }

  @Override
  public <T> void forEach(MongoQuery mongoQuery, Class<T> clazz, Consumer<T> consumer) {
    forEach(collectionName(clazz), mongoQuery, clazz, consumer);
  }

  @Override
  public <T> void forEach(String collectionName, MongoQuery mongoQuery, Class<T> clazz,
    Consumer<T> consumer) {
    mongoDao.forEach(database, collectionName, mongoQuery, clazz, consumer);
  }

  @Override
  public long count(MongoFilter filter, Class clazz) {
    return count(filter, collectionName(clazz));
  }

  @Override
  public long count(MongoFilter filter, String collectionName) {
    return mongoDao.count(database, collectionName, filter);
  }

  @Override
  public <P, T> UpdateResult update(P id, T doc) {
    return update(MongoFilter.create().key("_id").eq(id),
      mongoDao.getDocumentMapper().toMongoUpdate(doc),
      collectionName(doc.getClass()));
  }

  @Override
  public <P, T> UpdateResult update(P id, T doc, String collectionName) {
    return update(MongoFilter.create().key("_id").eq(id),
      mongoDao.getDocumentMapper().toMongoUpdate(doc),
      collectionName);
  }

  @Override
  public <P> UpdateResult update(P id, MongoUpdate update, Class clazz) {
    return update(id, update, collectionName(clazz));
  }

  @Override
  public <P> UpdateResult update(P id, MongoUpdate update, String collectionName) {
    return mongoDao.update(database, collectionName, id, update);
  }

  @Override
  public UpdateResult update(MongoFilter filter, MongoUpdate update, Class clazz) {
    return update(filter, update, collectionName(clazz));
  }

  @Override
  public UpdateResult update(MongoFilter filter, MongoUpdate update, Class clazz,
    WriteConcern writeConcern) {
    return update(filter, update, collectionName(clazz), writeConcern);
  }

  @Override
  public UpdateResult update(MongoFilter filter, MongoUpdate update,
    String collectionName) {
    return mongoDao.update(database, collectionName, filter, update);
  }

  @Override
  public UpdateResult update(MongoFilter filter, MongoUpdate update,
    String collectionName,
    WriteConcern writeConcern) {
    return mongoDao.update(database, collectionName, filter, update, writeConcern);
  }

  @Override
  public <T> T findOneAndUpdate(MongoQuery filter, MongoUpdate update,
    FindOneAndUpdateOptions options, Class<T> clazz) {
    return mongoDao.findOneAndUpdate(database, collectionName(clazz), filter,
      update, options, clazz);
  }

  @Override
  public <P> DeleteResult delete(P id, Class clazz) {
    return delete(id, collectionName(clazz));
  }

  @Override
  public <P> DeleteResult delete(P id, String collectionName) {
    return mongoDao.delete(database, collectionName, id);
  }

  @Override
  public DeleteResult delete(MongoFilter filter, Class clazz) {
    return delete(filter, collectionName(clazz));
  }

  @Override
  public DeleteResult delete(MongoFilter filter, Class clazz, WriteConcern writeConcern) {
    return delete(filter, collectionName(clazz), writeConcern);
  }

  @Override
  public DeleteResult delete(MongoFilter filter, String collectionName) {
    return mongoDao.delete(database, collectionName, filter);
  }

  @Override
  public DeleteResult delete(MongoFilter filter, String collectionName,
    WriteConcern writeConcern) {
    return mongoDao.delete(database, collectionName, filter, writeConcern);
  }

  @Override
  public MongoDatabase mongoDatabase() {
    return mongoDao.mongoDatabase(database);
  }

  private String collectionName(Class clazz) {
    String collectionName = collectionNames.get(clazz);
    if (collectionName == null) {
      synchronized (this) {
        Table annotation = (Table) clazz.getAnnotation(Table.class);
        if (annotation != null && (collectionName = annotation.name()) != null) {
        } else {
          collectionName = CaseFormat.LOWER_CAMEL
            .to(CaseFormat.LOWER_UNDERSCORE, clazz.getSimpleName());
        }
        collectionNames.putIfAbsent(clazz, collectionName);
      }
    }
    return collectionName;
  }

}
