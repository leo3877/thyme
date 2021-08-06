package org.yixi.thyme.data.mysql;


import org.jooq.DSLContext;
import org.yixi.data.client.QueryResponse;
import org.yixi.data.client.UpdateResponse;
import org.yixi.data.client.UpsertResponse;
import org.yixi.thyme.core.BaseEntity;
import org.yixi.thyme.data.BaseDao;

/**
 * @author yixi
 * @since 1.0.0
 */
@SuppressWarnings("all")
public interface MysqlBaseDao<PK, Entity extends BaseEntity<PK>>
  extends BaseDao<PK, Entity, MysqlQuery, MysqlFilter> {

  PK create(Entity entity, boolean onDuplicateKeyUpdate);

  UpdateResponse update(MysqlFilter filter, Entity entity);

  QueryResponse<Entity> find(MysqlQuery mysqlQuery);

  UpsertResponse<PK> update(MysqlFilter filter, Entity entity, boolean upsert);

  Entity findOneAndUpdate(MysqlQuery mysqlQuery, Entity entity);

  Entity findOneAndUpdate(MysqlQuery mysqlQuery, Entity entity, UpdateOption option);

  DSLContext dslContext();

  org.jooq.Table table();
}
