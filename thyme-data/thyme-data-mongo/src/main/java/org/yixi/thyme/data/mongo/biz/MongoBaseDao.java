package org.yixi.thyme.data.mongo.biz;

import org.bson.Document;
import org.yixi.data.client.UpdateResponse;
import org.yixi.thyme.core.BaseEntity;
import org.yixi.thyme.data.BaseDao;
import org.yixi.thyme.data.mongo.MongoDatabaseDao;
import org.yixi.thyme.data.mongo.MongoFilter;
import org.yixi.thyme.data.mongo.MongoQuery;

/**
 * @author yixi
 * @since 1.0.0
 */
@SuppressWarnings("all")
public interface MongoBaseDao<PK, Entity extends BaseEntity<PK>>
  extends BaseDao<PK, Entity, MongoQuery, MongoFilter> {

  UpdateResponse update(MongoFilter mongoFilter, Entity entity);

  // 深度更新
  UpdateResponse updateDeep(Entity entity);

  Entity findOneAndUpdate(MongoQuery mongoQuery, Entity entity);

  Entity findOneAndUpdate(MongoQuery mongoQuery, Entity entity, UpdateOption option);

  MongoDatabaseDao mongoDatabaseDao();

  Document toMongoDoc(Object obj);

}
