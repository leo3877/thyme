package org.yixi.thyme.data.mongo.biz;

import com.mongodb.MongoBulkWriteException;
import com.mongodb.assertions.Assertions;
import com.mongodb.bulk.BulkWriteError;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.result.UpdateResult;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import org.bson.Document;
import org.yixi.data.client.Fetch;
import org.yixi.data.client.QueryResponse;
import org.yixi.data.client.UpdateResponse;
import org.yixi.data.client.YixiQuery;
import org.yixi.thyme.core.BaseEntity;
import org.yixi.thyme.core.Batcher;
import org.yixi.thyme.core.ObjectId;
import org.yixi.thyme.core.Thyme;
import org.yixi.thyme.core.ex.DataInvalidException;
import org.yixi.thyme.core.ex.ThymeException;
import org.yixi.thyme.core.util.Validations;
import org.yixi.thyme.data.BaseDaoImpl;
import org.yixi.thyme.data.graphdata.DataFetcher;
import org.yixi.thyme.data.graphdata.GraphQuery;
import org.yixi.thyme.data.mongo.MongoFilter;
import org.yixi.thyme.data.mongo.MongoQuery;
import org.yixi.thyme.data.mongo.MongoUpdate;
import org.yixi.thyme.data.mongo.QueryHelper;

/**
 * @author yixi
 * @since 1.0.0
 */
@SuppressWarnings("all")
public abstract class MongoBaseDaoImpl<PK, Entity extends BaseEntity<PK>>
  extends BaseDaoImpl<PK, Entity, MongoQuery, MongoFilter>
  implements MongoBaseDao<PK, Entity> {

  private final QueryHelper queryHelper = new QueryHelper(this);

  private final GraphQuery graphQuery = new GraphQuery();

  private final List<DataReadListener<Entity>> dataReadListeners = new CopyOnWriteArrayList<>();
  private final List<DataCreateListener<Entity>> dataCreateListeners = new CopyOnWriteArrayList<>();
  private final List<DataUpdateListener<Entity>> dataUpdateListeners = new CopyOnWriteArrayList<>();
  private final List<DataReplaceListener<Entity>> dataReplaceListeners = new CopyOnWriteArrayList<>();
  private final List<DataDeleteListener<Entity>> dataDeleteListeners = new CopyOnWriteArrayList<>();

  @Override
  public void addDataCreateListener(DataCreateListener<Entity> dataCreateListener) {
    this.dataCreateListeners.add(dataCreateListener);
  }

  @Override
  public void addDataDeleteListener(DataDeleteListener dataDeleteListener) {
    this.dataDeleteListeners.add(dataDeleteListener);
  }

  @Override
  public void addDataReadListener(DataReadListener dataReadListener) {
    this.dataReadListeners.add(dataReadListener);
  }

  @Override
  public void addDataReplaceListener(DataReplaceListener dataReplaceListener) {
    this.dataReplaceListeners.add(dataReplaceListener);
  }

  @Override
  public void addDataUpdateListener(DataUpdateListener dataUpdateListener) {
    this.dataUpdateListeners.add(dataUpdateListener);
  }

  @Override
  public PK create(Entity entity) {
    Validations.validate(entity);
    Assertions.notNull("entity must be specified", entity);
    if (entity.getId() == null) {
      entity.setId(genNewId());
    }
    if (entity.getIsDeleted() == null) {
      entity.setIsDeleted(0);
    }
    for (DataCreateListener<Entity> dataCreateListener : dataCreateListeners) {
      dataCreateListener.beforeCreate(entity);
    }
    PK id = mongoDatabaseDao().insert(entity);
    for (DataCreateListener<Entity> dataCreateListener : dataCreateListeners) {
      dataCreateListener.afterCread(entity);
    }
    return id;
  }

  @Override
  public List<Object> create(List<Entity> entities) {
    Assertions.notNull("entities must be specified", entities);

    List<Object> ids = Thyme.newList(entities.size(), i -> {
      Entity entity = entities.get(i);
      Validations.validate(entity);
      if (entity.getId() != null) {
        return entity.getId();
      } else if (idType() != String.class) {
        throw new ThymeException("只有当 id 为 String 类型的时候，才可以不填写 id 值");
      } else {
        String id = new ObjectId().toHexString();
        entity.setId((PK) id);
        return (PK) id;
      }
    });
    if (entities.size() > 0) {
      try {
        mongoDatabaseDao().insert(entities);
      } catch (MongoBulkWriteException ex) {
        Map<Integer, Exception> exceptionMap = new LinkedHashMap<>();
        for (BulkWriteError error : ex.getWriteErrors()) {
          if (error.getCode() == 11000) {
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
            exceptionMap.put(error.getIndex(), new DataInvalidException(fields));
          } else {
            exceptionMap.put(error.getIndex(), new ThymeException(error.getMessage()));
          }
        }
        return (List<Object>) Thyme.newList(entities.size(), i -> {
          Exception exception = exceptionMap.get(i);
          if (exception != null) {
            return exception;
          } else {
            return ids.get(i);
          }
        });
      }
    }
    return ids;
  }

  @Override
  public long replace(Entity entity) {
    Assertions.notNull("entity must be specified", entity);
    Assertions.notNull("id must be specified", entity.getId());
    Validations.validate(entity);
    for (DataReplaceListener<Entity> dataReplaceListener : dataReplaceListeners) {
      dataReplaceListener.beforeReplace(entity);
    }
    long replace = mongoDatabaseDao().replace(entity.getId(), entity);
    if (replace > 0) {
      for (DataReplaceListener<Entity> dataReplaceListener : dataReplaceListeners) {
        dataReplaceListener.afterReplace(entity);
      }
    }
    return replace;
  }

  @Override
  public UpdateResponse update(Entity entity) {
    Assertions.notNull("entity must be specified", entity);
    Assertions.notNull("id must be specified", entity.getId());
    Validations.validate(entity, true);
    for (DataUpdateListener<Entity> dataUpdateListener : dataUpdateListeners) {
      dataUpdateListener.beforeUpdate(entity);
    }
    UpdateResult result = mongoDatabaseDao().update(entity.getId(), entity);
    if (result.getModifiedCount() > 0) {
      for (DataUpdateListener<Entity> dataUpdateListener : dataUpdateListeners) {
        dataUpdateListener.afterUpdate(entity);
      }
    }
    return new UpdateResponse(result.getMatchedCount(), result.getMatchedCount());
  }

  @Override
  public UpdateResponse update(MongoFilter mongoFilter, Entity entity) {
    Assertions.notNull("mongoQuery must be specified", mongoFilter);
    Assertions.notNull("entity must be specified", entity);
    UpdateResult result = mongoDatabaseDao()
      .update(mongoFilter, MongoUpdate.create().set(toMongoDoc(entity)), entityClass());
    return new UpdateResponse(result.getMatchedCount(), result.getMatchedCount());
  }

  @Override
  public UpdateResponse updateDeep(Entity entity) {
    Assertions.notNull("entity must be specified", entity);
    Assertions.notNull("id must be specified", entity.getId());
    UpdateResult result = mongoDatabaseDao()
      .update(entity.getId(), MongoUpdate.create().setDeep(toMongoDoc(entity)), entityClass());
    return new UpdateResponse(result.getMatchedCount(), result.getMatchedCount());
  }

  @Override
  public Entity findOneAndUpdate(MongoQuery mongoQuery, Entity entity) {
    return findOneAndUpdate(mongoQuery, entity, null);
  }

  @Override
  public Entity findOneAndUpdate(MongoQuery mongoQuery, Entity entity,
    UpdateOption option) {
    FindOneAndUpdateOptions updateOptions = null;
    MongoUpdate mongoUpdate;
    if (option != null) {
      updateOptions = new FindOneAndUpdateOptions();
      updateOptions.upsert(option.isUpset());
      if (option.isReturnAfter()) {
        updateOptions.returnDocument(ReturnDocument.AFTER);
      }
      if (option.isDeep()) {
        mongoUpdate = MongoUpdate.create().setDeep(toMongoDoc(entity));
      } else {
        mongoUpdate = MongoUpdate.create().set(toMongoDoc(entity));
      }
    } else {
      mongoUpdate = MongoUpdate.create().setDeep(toMongoDoc(entity));
    }

    return mongoDatabaseDao()
      .findOneAndUpdate(mongoQuery, mongoUpdate, updateOptions, entityClass());
  }

  @Override
  public Entity get(PK id) {
    Assertions.notNull("id must be specified", id);
    List<Entity> entities = doFind(MongoQuery.create().key("id").eq(id), ReadType.Get);
    if (entities.isEmpty()) {
      return null;
    } else {
      return entities.get(0);
    }
  }

  @Override
  public long deleteById(PK id) {
    Assertions.notNull("id must be specified", id);
    if (!dataDeleteListeners.isEmpty()) {
      Entity entity = get(id);
      if (entity != null) {
        for (DataDeleteListener<Entity> dataDeleteListener : dataDeleteListeners) {
          dataDeleteListener.beforeDelete(entity);
        }
        long deletedCount = mongoDatabaseDao().delete(id, entityClass()).getDeletedCount();
        for (DataDeleteListener<Entity> dataDeleteListener : dataDeleteListeners) {
          dataDeleteListener.afterDelete(entity);
        }
        return deletedCount;
      } else {
        return 0;
      }
    } else {
      return mongoDatabaseDao().delete(id, entityClass()).getDeletedCount();
    }
  }

  @Override
  public long delete(MongoFilter filter) {
    return mongoDatabaseDao().delete(filter, entityClass()).getDeletedCount();
  }

  @Override
  public Entity findOne(MongoQuery query) {

    List<Entity> list = doFind(query, ReadType.FindOne);
    if (list.isEmpty()) {
      return null;
    } else {
      return list.get(0);
    }
  }

  @Override
  public List<Entity> findMany(MongoQuery query) {
    return doFind(query, ReadType.FindMany);
  }

  private List<Entity> doFind(MongoQuery query, ReadType readType) {
    Assertions.notNull("query must be specified", query);
    if (readType == ReadType.FindOne) {
      query.limit(1);
    }
    for (DataReadListener<Entity> dataReadListener : dataReadListeners) {
      dataReadListener.beforeRead(readType, null);
    }
    List<Entity> entities = mongoDatabaseDao().find(query, entityClass());
    for (Entity entity : entities) {
      for (DataReadListener<Entity> dataReadListener : dataReadListeners) {
        dataReadListener.afterRead(readType, entity);
      }
    }
    return entities;
  }

  @Override
  public void forEach(MongoQuery baseQuery, Consumer<Entity> consumer) {
    mongoDatabaseDao().forEach(baseQuery, entityClass(), consumer);
  }

  @SuppressWarnings("all")
  public void forEach(YixiQuery query, Consumer<List<Entity>> consumer) {
    Batcher<Entity, Void> batcher = new Batcher<Entity, Void>(200) {
      @Override
      public List<Void> run(List<Entity> buffer) {
        Map<String, Fetch> fetchs = query.getFechs();
        if (fetchs != null) {
          graphData(buffer, fetchs);
        }
        consumer.accept(buffer);
        return null;
      }
    };
    forEach(QueryHelper.from(query), batcher::addObject);
    batcher.flush();
  }

  @Override
  public long count(MongoFilter filter) {
    return mongoDatabaseDao().count(filter, entityClass());
  }

  @Override
  public QueryResponse<Entity> query(MongoQuery query) {
    Assertions.notNull("query must be specified", query);
    return queryHelper.query(query);
  }

  public QueryResponse query(YixiQuery query) {
    Assertions.notNull("query must be specified", query);
    QueryResponse<Entity> response = queryHelper.query(query);
    Map<String, Fetch> fetchs = query.getFechs();
    if (fetchs != null) {
      graphData(response.getObjects(), fetchs);
    }
    return response;
  }

  @Override
  public void graphData(Object object, Map<String, Fetch> fetchs) {
    graphQuery.fetch(object, fetchs);
  }

  @Override
  public void graphData(List<Object> objects, Map<String, Fetch> fetchs) {
    throw Thyme.exUnsupported("不支持该方法");
  }

  @Override
  public Document toMongoDoc(Object obj) {
    return mongoDatabaseDao().getDocumentMapper().toDocument(obj);
  }

  public void registerFetcher(String field, DataFetcher dataFetcher) {
    graphQuery.registerFetcher(field, dataFetcher);
  }
}
