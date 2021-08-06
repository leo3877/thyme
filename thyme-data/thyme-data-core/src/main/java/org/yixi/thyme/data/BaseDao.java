package org.yixi.thyme.data;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.Data;
import org.yixi.data.client.BulkRequest;
import org.yixi.data.client.BulkResponse;
import org.yixi.data.client.Fetch;
import org.yixi.data.client.QueryResponse;
import org.yixi.data.client.UpdateResponse;
import org.yixi.thyme.core.BaseEntity;

/**
 * @author yixi
 * @since 1.0.0
 */
@SuppressWarnings("all")
public interface BaseDao<PK, Entity extends BaseEntity<PK>, Query extends BaseQuery, Filter extends BaseQuery.Filter> {

  void addDataReadListener(DataReadListener<Entity> dataReadListener);

  void addDataCreateListener(DataCreateListener<Entity> dataCreateListener);

  void addDataUpdateListener(DataUpdateListener<Entity> dataUpdateListener);

  void addDataReplaceListener(DataReplaceListener<Entity> dataReplaceListener);

  void addDataDeleteListener(DataDeleteListener<Entity> dataDeleteListener);

  /**
   * 创建一个
   */
  PK create(Entity entity);

  /**
   * 批量创建
   */
  List<Object> create(List<Entity> entities);

  /**
   * 全量替换
   */
  long replace(Entity entity);

  /**
   * 增量更新
   */
  UpdateResponse update(Entity entity);

  /**
   * 根据 id 删除
   */
  long deleteById(PK id);

  /**
   * 根据查询条件删除
   */
  long delete(Filter filter);

  /**
   * 通过 id 获取
   */
  Entity get(PK id);

  /**
   * 查询一个
   */
  Entity findOne(Query query);

  /**
   * 查询多个
   */
  List<Entity> findMany(Query query);

  /**
   * 迭代所有
   */
  void forEach(Query query, Consumer<Entity> consumer);

  /**
   * 返回总数
   */
  long count(Filter filer);

  QueryResponse<Entity> query(Query query);

  /**
   * 批量操作
   */
  BulkResponse bulk(BulkRequest<Entity> bulkRequest);

  void graphData(Object object, Map<String, Fetch> fetchs);

  void graphData(List<Object> objects, Map<String, Fetch> fetchs);

  PK genNewId();

  Class<Entity> entityClass();

  Class<PK> idType();

  /**
   * @author yixi
   * @since 1.0.1
   */
  @Data
  class UpdateOption {

    private boolean upset;
    private boolean deep; // 深度更新
    private boolean returnAfter; // 返回修改后的数据

  }

  /**
   * @author yixi
   */
  public interface DataReadListener<T> {

    void beforeRead(ReadType readType, T obj);

    void afterRead(ReadType readType, T obj);
  }

  /**
   * @author yixi
   */
  public interface DataDeleteListener<T> {

    void beforeDelete(T obj);

    void afterDelete(T obj);
  }

  /**
   * @author yixi
   */
  public interface DataCreateListener<T> {

    void beforeCreate(T obj);

    void afterCread(T obj);
  }

  /**
   * @author yixi
   */
  public interface DataUpdateListener<T> {

    void beforeUpdate(T obj);

    void afterUpdate(T obj);
  }

  /**
   * @author yixi
   */
  public interface DataReplaceListener<T> {

    void beforeReplace(T obj);

    void afterReplace(T obj);
  }


  /**
   * @author yixi
   */
  public enum ReadType {

    Get,

    FindOne,

    FindMany,

    ForEach

  }

}
