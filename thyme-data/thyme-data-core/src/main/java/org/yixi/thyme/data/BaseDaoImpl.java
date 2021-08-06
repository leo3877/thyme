package org.yixi.thyme.data;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.yixi.data.client.BulkRequest;
import org.yixi.data.client.BulkResponse;
import org.yixi.thyme.core.BaseEntity;
import org.yixi.thyme.core.ex.ThymeException;

/**
 * @author yixi
 * @since 1.0.0
 */
@SuppressWarnings("all")
public abstract class BaseDaoImpl<PK, Entity extends BaseEntity<PK>, Query extends BaseQuery,
  Filter extends BaseQuery.Filter> implements
  BaseDao<PK, Entity, Query, Filter> {

  private volatile Class idClass;

  @Override
  public BulkResponse bulk(BulkRequest<Entity> bulkRequest) {
    return BulkOperator.handle((BaseDao) this, bulkRequest);
  }

  @Override
  public Class idType() {
    if (idClass == null) {
      Type type = entityClass().getGenericSuperclass();
      if (type instanceof ParameterizedType) {
        idClass = (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
      } else {
        throw new ThymeException("EntityClass 类型不对，必须是：ParameterizedType 类型");
      }
    }
    return idClass;
  }
}
