package org.yixi.thyme.data;

import java.util.List;
import org.yixi.data.client.BulkRequest;
import org.yixi.data.client.BulkResponse;
import org.yixi.thyme.core.BaseEntity;
import org.yixi.thyme.core.CustomClassLoader;
import org.yixi.thyme.core.Document;
import org.yixi.thyme.core.ex.ThymeException;
import org.yixi.thyme.core.util.ReflectionUtils;
import org.yixi.thyme.data.BaseQuery.Filter;

/**
 * 大批量操作
 *
 * @author yixi
 * @since 1.0.0
 */
@SuppressWarnings("all")
public abstract class BulkOperator {

  public static final int DEFAULT_BATCH_SIZE = 500;

  public static <PK, Entity extends BaseEntity<PK>> BulkResponse handle(
    BaseDao<PK, Entity, BaseQuery, Filter> baseDao,
    BulkRequest<Entity> bulkRequest) {
    if (bulkRequest == null) {
      throw new ThymeException("batch must be specified");
    }
    if (bulkRequest.size() > DEFAULT_BATCH_SIZE) {
      throw new ThymeException(
        "Batch operations can't be greater than " + DEFAULT_BATCH_SIZE);
    }
    return new BulkProcessor(baseDao, bulkRequest).exec();
  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  public static class BulkProcessor<PK, Entity extends BaseEntity<PK>> {

    private final BaseDao<PK, Entity, BaseQuery, Filter> baseDao;
    private final BulkRequest<Entity> bulkRequest;

    private final static CustomClassLoader customClassLoader = new CustomClassLoader();

    public BulkProcessor(BaseDao<PK, Entity, BaseQuery, Filter> baseDao,
      BulkRequest<Entity> bulkRequest) {
      this.baseDao = baseDao;
      this.bulkRequest = bulkRequest;
    }

    public BulkResponse exec() {
      BulkResponse bulkResponse = new BulkResponse();
      // create
      try {
        List<Object> createRes = baseDao.create(bulkRequest.getCreates());
        for (Object o : createRes) {
          bulkResponse.getCreates().add(o);
        }
      } catch (Exception e) {
        bulkResponse.getCreates()
          .add(propagateAndTranslateEx(bulkRequest.isTransactional(), e));
      }

      // update
      for (Entity entity : bulkRequest.getUpdates()) {
        try {
          bulkResponse.getUpdates()
            .add(new Document<>(entity.getId(), baseDao.update(entity)));
        } catch (Exception e) {
          bulkResponse.getUpdates()
            .add(propagateAndTranslateEx(bulkRequest.isTransactional(), e));
        }
      }

      // replace
      for (Entity entity : bulkRequest.getReplaces()) {
        try {
          bulkResponse.getReplaces()
            .add(new Document<>(entity.getId(), baseDao.replace(entity)));
        } catch (Exception e) {
          bulkResponse.getReplaces().add(
            propagateAndTranslateEx(bulkRequest.isTransactional(), e));
        }
      }

      // delete
      List<Object> deletes = bulkRequest.getDeletes();
      if (deletes.size() > 0) {
        try {
          BaseQuery.Filter filter;
          if ("MongoBaseManagerImpl"
            .equals(baseDao.getClass().getSuperclass().getSimpleName())) {
            filter = (BaseQuery.Filter) ReflectionUtils.newInstance(
              customClassLoader.loadClass("org.yixi.thyme.mongo.MongoQuery$MongoFilter"));
          } else if ("MysqlBaseManagerImpl"
            .equals(baseDao.getClass().getSuperclass().getSimpleName())) {
            filter = (BaseQuery.Filter) ReflectionUtils.newInstance(
              customClassLoader.loadClass("org.yixi.thyme.mysql.MysqlQuery$MysqlFilters"));
          } else {
            throw new ThymeException("不支持 Bulk 操作.");
          }
          bulkResponse
            .setDeletes(new Document<>("amount", baseDao.delete(filter.key("id").in(deletes))));
        } catch (Exception e) {
          bulkResponse.setDeletes(propagateAndTranslateEx(bulkRequest.isTransactional(), e));
        }
      }
      return bulkResponse;
    }

    public ThymeException propagateAndTranslateEx(boolean transactional, Exception e) {
      ThymeException ex;
      if (e instanceof ThymeException) {
        ex = (ThymeException) e;
      } else {
        ex = new ThymeException(e.getMessage());
      }
      if (transactional) {
        throw ex;
      } else {
        return ex;
      }
    }
  }
}
