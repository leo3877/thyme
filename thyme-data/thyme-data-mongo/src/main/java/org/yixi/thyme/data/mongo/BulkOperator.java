package org.yixi.thyme.data.mongo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.yixi.data.client.BulkRequest;
import org.yixi.data.client.BulkResponse;
import org.yixi.thyme.core.BaseEntity;
import org.yixi.thyme.core.Document;
import org.yixi.thyme.core.ex.DataInvalidException;
import org.yixi.thyme.core.ex.ErrorType;
import org.yixi.thyme.core.ex.ThymeException;
import org.yixi.thyme.core.util.Validations;
import org.yixi.thyme.data.BaseDao;
import org.yixi.thyme.data.BaseQuery;
import org.yixi.thyme.data.BaseQuery.Filter;
import org.yixi.thyme.data.mongo.biz.MongoBaseDao;

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
    MongoBaseDao<PK, Entity> mongoBaseManager,
    BulkRequest<Entity> bulkRequest) {
    if (bulkRequest == null) {
      throw new ThymeException("batch must be specified");
    }
    if (bulkRequest.size() > DEFAULT_BATCH_SIZE) {
      throw new ThymeException(
        "Batch operations can't be greater than " + DEFAULT_BATCH_SIZE);
    }
    return new BulkProcessor(mongoBaseManager, bulkRequest).exec();
  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  public static class BulkProcessor<PK, Entity extends BaseEntity<PK>> {

    private final BaseDao<PK, Entity, BaseQuery, Filter> baseDao;
    private final BulkRequest<Entity> bulkRequest;

    public BulkProcessor(BaseDao<PK, Entity, BaseQuery, Filter> baseDao,
      BulkRequest<Entity> bulkRequest) {
      this.baseDao = baseDao;
      this.bulkRequest = bulkRequest;
    }

    public BulkResponse exec() {
      BulkResponse bulkResponse = new BulkResponse();
      List<Object> createRes = baseDao.create(bulkRequest.getCreates());
      for (Object o : createRes) {
        if (!(o instanceof Exception)) {
          bulkResponse.getCreates().add(o);
        } else {
          bulkResponse.getCreates().add(new Document<>("id", o));
        }
      }
      parallelExec(bulkRequest.getUpdates(), bulkResponse.getUpdates(),
        obj -> {
          Validations.validate(obj, true);
          return new Document(((BaseEntity) obj).getId(),
            baseDao.update((Entity) obj));
        });
      parallelExec(bulkRequest.getReplaces(), bulkResponse.getReplaces(),
        obj -> {
          Validations.validate(obj);
          return new Document(((BaseEntity) obj).getId(),
            baseDao.replace((Entity) obj));
        });
      List<Object> res = new ArrayList<>();
      bulkResponse.setDeletes(res);
      parallelExec(bulkRequest.getDeletes(), res,
        id -> new Document(id, baseDao.deleteById((PK) id)));
      return bulkResponse;
    }

    private void parallelExec(List<?> requests, List<Object> res,
      Function<Object, Document<String, Object>> fn) {
      for (Object obj : requests) {
        try {
          res.add(fn.apply(obj));
        } catch (DataInvalidException ex) {
          res.add(new Document<>().append("code", ex.getCode())
            .append("message", ex.getMessage())
            .append("fields", ex.getKeys()));
        } catch (Exception e) {
          res.add(new Document<String, Object>("code", ErrorType.INNER_ERROR.getCode())
            .append("message", e.getMessage()));
        }
      }
    }
  }
}
