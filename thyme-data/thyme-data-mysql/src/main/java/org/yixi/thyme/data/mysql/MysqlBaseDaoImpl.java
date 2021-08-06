package org.yixi.thyme.data.mysql;

import com.google.common.collect.Maps;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.apache.commons.lang3.StringUtils;
import org.jooq.Condition;
import org.jooq.InsertSetMoreStep;
import org.jooq.JoinType;
import org.jooq.Record;
import org.jooq.SelectJoinStep;
import org.jooq.SelectSelectStep;
import org.jooq.SortField;
import org.jooq.TableField;
import org.jooq.UpdatableRecord;
import org.springframework.transaction.annotation.Transactional;
import org.yixi.data.client.BulkRequest;
import org.yixi.data.client.BulkResponse;
import org.yixi.data.client.Fetch;
import org.yixi.data.client.QueryResponse;
import org.yixi.data.client.UpdateResponse;
import org.yixi.data.client.UpsertResponse;
import org.yixi.data.client.YixiQuery;
import org.yixi.thyme.core.BaseEntity;
import org.yixi.thyme.core.Document;
import org.yixi.thyme.core.Thyme;
import org.yixi.thyme.core.ex.BusinessException;
import org.yixi.thyme.core.ex.ThymeException;
import org.yixi.thyme.core.json.Jsons;
import org.yixi.thyme.core.util.Assertions;
import org.yixi.thyme.core.util.ReflectionUtils;
import org.yixi.thyme.core.util.Validations;
import org.yixi.thyme.data.BaseDaoImpl;
import org.yixi.thyme.data.Constants;
import org.yixi.thyme.data.graphdata.DataFetcher;
import org.yixi.thyme.data.graphdata.GraphQuery;
import org.yixi.thyme.data.graphdata.LocalDataFetcher;
import org.yixi.thyme.data.type.TypeConverters;

/**
 * @author yixi
 * @since 1.0.0
 */
@SuppressWarnings("all")
public abstract class MysqlBaseDaoImpl<PK, Entity extends BaseEntity<PK>>
  extends BaseDaoImpl<PK, Entity, MysqlQuery, MysqlFilter>
  implements MysqlBaseDao<PK, Entity> {

  private final GraphQuery graphQuery = new GraphQuery();
  private final TypeConverters typeConverters = new TypeConverters(
    LocalDataFetcher.DataSourceType.Mysql);

  private final JooqClassMeta jooqClassMeta = JooqTables.classMetaRequire(entityClass());

  @Override
  public PK create(Entity entity) {
    doCreate(entity, false);
    if (entity.getId() != null) {
      return entity.getId();
    }
    long lastID = dslContext().lastID().longValue();
    if (table().field("id").getDataType().getType() == Integer.class) {
      return (PK) (Integer) (int) lastID;
    } else {
      return (PK) (Long) lastID;
    }
  }

  @Override
  @Transactional
  public PK create(Entity entity, boolean onDuplicateKeyUpdate) {
    int res = doCreate(entity, onDuplicateKeyUpdate);
    if (entity.getId() != null) {
      return entity.getId();
    }
    if (res == 1) {
      long lastID = dslContext().lastID().longValue();
      if (table().field("id").getDataType().getType() == Integer.class) {
        return (PK) (Integer) (int) lastID;
      } else {
        return (PK) (Long) lastID;
      }
    }
    MysqlQuery query = MysqlQuery.create();
    tableFieldValues(entity).forEach((k, v) -> {
      query.key(k.getName()).eq(v);
    });
    return findOne(query).getId();
  }

  @Override
  public List<Object> create(List<Entity> entities) {
    List<Object> res = new ArrayList<>();
    for (Entity entity : entities) {
      res.add(create(entity));
    }
    return res;
  }

  @Override
  @Transactional
  public long replace(Entity entity) {
    Assertions.notNull("entity.id must be specified", entity.getId());
    Validations.validate(entity);

    org.jooq.Table table = table();
    org.jooq.Field id = table.field("id");

    if (id == null) {
      throw new IllegalArgumentException("数据库表 id 字段不存在: " + table);
    }

    Collection selectFields = new ArrayList<>();
    selectFields.add(id);
    if (table.field("gmt_created") != null) {
      selectFields.add(table.field("gmt_created"));
    }

    Record record = dslContext()
      .select(selectFields)
      .from(table)
      .where(id.eq(entity.getId()))
      .fetchOne();

    if (record == null) {
      return 0;
    }

    dslContext().delete(table).where(id.eq(entity.getId())).execute();
    Timestamp createdTime = (Timestamp) record.get(BaseEntity.CREATE_TIME);
    if (createdTime != null) {
      entity.setCreateTime(new Date(createdTime.getTime()));
    }
    create(entity);

    return 1;
  }

  @Override
  public UpdateResponse update(Entity entity) {
    Assertions.notNull("entity must be specified", entity);
    Assertions.notNull("entity.id must be specified", entity.getId());
    Validations.validate(entity, true);

    UpdatableRecord record = (UpdatableRecord) dslContext().newRecord(table());
    tableFieldValues(entity).forEach((f, v) -> {
      if (!"id".equals(f.getName())) {
        record.set(f, v);
      }
    });
    if (!record.changed()) {
      return new UpdateResponse(0, 0);
    }
    int c = dslContext().executeUpdate(record, table().field("id").eq(entity.getId()));
    return new UpdateResponse(c, c);
  }

  @Override
  public UpdateResponse update(MysqlFilter filter, Entity entity) {
    Assertions.notNull("filter must be specified", filter);
    Assertions.notNull("entity must be specified", entity);
    Validations.validate(entity, true);
    UpdatableRecord record = (UpdatableRecord) dslContext().newRecord(table());
    tableFieldValues(entity).forEach((f, v) -> record.set(f, v));
    if (!record.changed()) {
      return new UpdateResponse(0, 0);
    }
    int c = dslContext().executeUpdate(record,
      new ConditionParser(jooqClassMeta).parseCondition(filter.doc()));
    return new UpdateResponse(c, c);
  }

  @Override
  @Transactional
  public UpsertResponse<PK> update(MysqlFilter filter, Entity entity, boolean upsert) {
    Assertions.notNull("filter must be specified", filter);
    Assertions.notNull("entity must be specified", entity);

    Validations.validate(entity, true);
    typeConverters.convert(filter.doc());

    Map<org.jooq.Field, Object> tableFields = tableFieldValues(entity);
    if (tableFields.isEmpty()) {
      throw new BusinessException("Update 内容不能为空.");
    }
    UpdateSql updateSql = new UpdateSql(table().getName(), tableFields,
      new ConditionParser(jooqClassMeta).parseFilter(filter.doc()), 1);
    int i = dslContext().execute(updateSql.toString());
    if (i <= 0 && upsert) {
      filter.doc().forEach((k, v) -> {
        Object val = null;
        if (v instanceof Map) {
          val = ((Map) v).get("$eq");
        } else {
          val = k;
        }
        if (val != null) {
          Field field = ReflectionUtils.findField(entity.getClass(), k);
          if (field != null) {
            ReflectionUtils.makeAccessible(field);
            try {
              if (field.get(entity) == null) {
                if (val instanceof Number && (field.getType() == Long.class
                  || field.getType() == long.class)) {
                  ReflectionUtils.setField(field, entity,
                    ((Number) val).longValue());
                } else if (val instanceof Number && (field.getType() == Double.class
                  || field.getType() == double.class)) {
                  ReflectionUtils.setField(field, entity,
                    ((Number) val).doubleValue());
                } else if (val instanceof Number && (field.getType() == Float.class
                  || field.getType() == float.class)) {
                  ReflectionUtils.setField(field, entity,
                    ((Number) val).floatValue());
                } else if (field.getType().isEnum()) {
                  ReflectionUtils.setField(field, entity,
                    Enum.valueOf((Class) field.getType(), val.toString()));
                }
              }
            } catch (Exception e) {
              throw new ThymeException(e.getMessage());
            }
          }
        }
      });
      int res = doCreate(entity, true);
      if (res == 1) {
        if (entity.getId() != null) {
          return UpsertResponse.create(null, entity.getId());
        }
        long lastID = dslContext().lastID().longValue();
        if (table().field("id").getDataType().getType() == Integer.class) {
          return UpsertResponse.create(null, (PK) (Integer) (int) lastID);
        } else {
          return UpsertResponse.create(null, (PK) (Long) lastID);
        }
      } else {
        return UpsertResponse.create(res == 2 ? 1 : res, null);
      }
    }
    return UpsertResponse.create(i, null);
  }

  @Override
  public Entity get(PK id) {
    new MysqlQuery().key("id").eq(id);
    List<Entity> entities = findMany(new MysqlQuery().key("id").eq(id));
    if (entities.isEmpty()) {
      return null;
    } else {
      return entities.get(0);
    }
  }

  @Override
  public long deleteById(PK id) {
    return dslContext().deleteFrom(table()).where(table().field("id").eq(id)).execute();
  }

  @Override
  public Entity findOne(MysqlQuery mysqlQuery) {
    List<Entity> list = findMany(mysqlQuery);
    if (list.isEmpty()) {
      return null;
    } else {
      return list.get(0);
    }
  }

  @Override
  @Transactional
  public Entity findOneAndUpdate(MysqlQuery mysqlQuery, Entity entity) {
    return findOneAndUpdate(mysqlQuery, entity, null);
  }

  @Override
  @Transactional
  public Entity findOneAndUpdate(MysqlQuery mysqlQuery, Entity entity, UpdateOption option) {
    Assertions.notNull("mysqlQuery must be specified", mysqlQuery);
    Assertions.notNull("entity must be specified", entity);
    entity.setId(null);
    if (option != null && option.isReturnAfter()) {
      update(mysqlQuery.filter(), entity);
      return findOne(mysqlQuery);
    } else {
      Entity one = findOne(mysqlQuery.copy().setSelectForUpdate(true));
      if (one != null) {
        update(mysqlQuery.filter(), entity);
      }
      return one;
    }
  }

  @Override
  public List<Entity> findMany(MysqlQuery mysqlQuery) {
    return doFind(mysqlQuery);
  }

  @Override
  public QueryResponse<Entity> find(MysqlQuery mysqlQuery) {
    QueryResponse findResponse = new QueryResponse();
    if (mysqlQuery.isCount()) {
      Condition condition = new ConditionParser(jooqClassMeta).parseCondition(
        mysqlQuery.filter().doc());
      if (condition != null) {
        findResponse.setAmount(
          dslContext().selectCount()
            .from(table())
            .where(condition)
            .fetchOne()
            .into(Long.class));
      } else {
        findResponse.setAmount(
          dslContext().selectCount().from(table()).fetchOne().into(Long.class));
      }
    }
    if (findResponse.getAmount() == null || findResponse.getAmount() > 0) {
      findResponse.setObjects(doFind(mysqlQuery));
    }
    findResponse.setLimit(mysqlQuery.limit());
    findResponse.setSkip(mysqlQuery.skip());
    return findResponse;
  }

  @Override
  public void forEach(MysqlQuery query, Consumer<Entity> consumer) {
    doFind(query).forEach(consumer);
  }

  @Override
  public long delete(MysqlFilter filter) {
    Assertions.notNull("filter must be specified", filter);
    return dslContext().deleteFrom(table())
      .where(new ConditionParser(jooqClassMeta).parseCondition(filter.doc())).execute();
  }

  @Override
  public long count(MysqlFilter filter) {
    Assertions.notNull("filter must be specified", filter);
    return dslContext().selectCount().from(table()).fetchOne().into(Long.class);
  }

  public QueryResponse query(YixiQuery query) {
    QueryResponse<Entity> response = find(from(query));
    Map<String, Fetch> fetchs = query.getFechs();
    if (fetchs != null) {
      graphData(response.getObjects(), fetchs);
    }
    return response;
  }

  @Override
  @Transactional
  public BulkResponse bulk(BulkRequest<Entity> bulkRequest) {
    return super.bulk(bulkRequest);
  }

  @Override
  public void graphData(Object object, Map<String, Fetch> fetchs) {
    graphQuery.fetch(object, fetchs);
  }

  public void registerFetcher(String field, DataFetcher dataFetcher) {
    graphQuery.registerFetcher(field, dataFetcher);
  }

  protected Map<org.jooq.Field, Object> tableFieldValues(Entity entity) {
    Map<String, Field> fields = jooqClassMeta.getFieldMap();
    org.jooq.Table table = jooqClassMeta.getTable();

    Map<org.jooq.Field, Object> tables = Maps.newLinkedHashMap();
    fields.forEach((k, v) -> {
      ReflectionUtils.makeAccessible(v);
      Object val = ReflectionUtils.getField(v, entity);
      if (val == null) {
        return;
      }
      org.jooq.Field field = jooqClassMeta.getTableField(k);
      if (field != null) {
        if (val instanceof Map || val instanceof List) {
          val = Jsons.encode(val);
        }
        tables.put(field, val);
      }
    });
    return tables;
  }

  private int doCreate(Entity entity, boolean onDuplicateKeyUpdate) {
    Assertions.notNull("entity must be specified", entity);
    Validations.validate(entity);
    Map<org.jooq.Field, Object> fieldObjectMap = tableFieldValues(entity);
    InsertSetMoreStep insertSetMoreStep = dslContext().insertInto(table()).set(fieldObjectMap);
    if (onDuplicateKeyUpdate) {
      return insertSetMoreStep.onDuplicateKeyUpdate().set(fieldObjectMap).execute();
    } else {
      return insertSetMoreStep.execute();
    }
  }

  private List<Entity> doFind(MysqlQuery mysqlQuery) {
    QueryContext queryContext = new QueryContext(mysqlQuery, jooqClassMeta);
    Map<String, TableField> tableFieldMap = jooqClassMeta.filterTableFields(
      mysqlQuery.fields());
    SelectSelectStep select = dslContext().select((Collection) tableFieldMap.values());
    SelectJoinStep selectJoinStep = select.from(jooqClassMeta.getTable());
    Condition condition = queryContext.condition();
    if (condition != null) {
      selectJoinStep.where(condition);
    }

    join(mysqlQuery.joins(), selectJoinStep, queryContext);
    selectJoinStep.orderBy(queryContext.sorts());

    int limit = mysqlQuery.limit();
    if (limit <= 0) {
      limit = Constants.DEFAULT_LIMIT;
    } else if (limit > Constants.DEFAULT_MAX_LIMIT) {
      throw new ThymeException(
        String.format("Exceeds limit, max limit is %d, but your limit is %d",
          Constants.DEFAULT_MAX_LIMIT, limit));
    }
    selectJoinStep.limit(limit);
    if (mysqlQuery.skip() > 0) {
      selectJoinStep.offset(mysqlQuery.skip());
    }
    if (mysqlQuery.isSelectForUpdate()) {
      selectJoinStep.forUpdate();
    }
    Map<Object, Entity> exists = new HashMap<>();
    List<Entity> entities = new ArrayList<>();
    selectJoinStep.forEach(r -> {
      Record record = (Record) r;
      Object id = record.get(jooqClassMeta.getTableField("id"));
      if (id == null) {
        return;
      }
      Entity exist = exists.get(id + jooqClassMeta.getClassName());
      if (exist == null) {
        exist = toObject(tableFieldMap, record, jooqClassMeta.getFieldMap(), entityClass());
        exists.put(id + jooqClassMeta.getClassName(), exist);
        entities.add(exist);
      }
      List<MysqlQuery.Join> joins = mysqlQuery.joins();
      buildEntityOnJoin(joins, record, exists, jooqClassMeta, exist);
    });
    return entities;
  }

  private void buildEntityOnJoin(List<MysqlQuery.Join> joins, Record record,
    Map<Object, Entity> exists, JooqClassMeta parentClassMeta, Object parent) {
    for (MysqlQuery.Join join : joins) {
      JooqClassMeta classMeta = JooqTables.classMetaRequire(join.clazz());
      Object childId = record.get(classMeta.getTableField("id"));
      Entity childExist = exists.get(childId + classMeta.getClassName());
      if (childExist == null) {
        childExist = toObject(join.getTableFieldMap(), record,
          classMeta.getFieldMap(), classMeta.getClazz());
        exists.put(childId + classMeta.getClassName(), childExist);
        Field field = parentClassMeta.getField(join.ref);
        if (field != null) {
          if (List.class.isAssignableFrom(field.getType())) {
            Type type = field.getGenericType();
            Class acturalType;
            if (type instanceof ParameterizedType) {
              acturalType = (Class) ((ParameterizedType) type)
                .getActualTypeArguments()[0];
            } else {
              throw new ThymeException(
                field.getName()
                  + " 类型不对，必须是：ParameterizedType 类型, "
                  + "例如：List<String>");
            }
            if (acturalType != classMeta.getClazz()) {
              throw new ThymeException(
                String.format("类型不匹配, acturalType: %s but: %s",
                  field.toString(),
                  classMeta.getClass().toString()));
            }
            List list = (List) ReflectionUtils.getField(field, parent);
            if (list != null) {
              list.add(childExist);
            } else {
              list = new ArrayList<>();
              list.add(childExist);
              ReflectionUtils.setField(field, parent, list);
            }
          } else {
            if (field.getType() != classMeta.getClazz()) {
              throw new ThymeException(
                String.format("类型不匹配, acturalType: %s but: %s",
                  field.toString(),
                  classMeta.getClass().toString()));
            }
            ReflectionUtils.setField(field, parent, childExist);
          }
        }
      }
      buildEntityOnJoin(join.children(), record, exists, classMeta, childExist);
    }
  }

  private void join(List<MysqlQuery.Join> joins, SelectJoinStep selectJoinStep,
    QueryContext queryContext) {
    for (MysqlQuery.Join join : joins) {
      JoinType joinType = JoinType.JOIN;
      if (join.joinType() == MysqlQuery.JoinType.LEFT) {
        joinType = JoinType.LEFT_OUTER_JOIN;
      } else if (join.joinType() == MysqlQuery.JoinType.RIGHT) {
        joinType = JoinType.RIGHT_OUTER_JOIN;
      } else if (join.joinType() == MysqlQuery.JoinType.OUTER) {
        joinType = JoinType.FULL_OUTER_JOIN;
      }
      JooqClassMeta classMeta = JooqTables.classMetaRequire(join.clazz());
      selectJoinStep.join(classMeta.getTable(), joinType)
        .on(queryContext.conditionParser.parseCondition(join.on().doc()));
      Map<String, TableField> tableFieldMap = classMeta.filterTableFields(join.fields());
      join.setTableFieldMap(tableFieldMap);
      ((SelectSelectStep) selectJoinStep).select(tableFieldMap.values());
      join(join.children(), selectJoinStep, queryContext);
    }
  }

  private Entity toObject(Map<String, TableField> tableFieldMap, Record record,
    Map<String, Field> fields, Class clazz) {
    Object entity = ReflectionUtils.newInstance(clazz);
    tableFieldMap.forEach((k, f) -> {
      Object val = record.get(f);
      if (val != null) {
        Field field = fields.get(k);
        if (val instanceof Number) {
          val = convert((Number) val, field.getType());
        }
        if (List.class.isAssignableFrom(field.getType()) && val instanceof String) {
          Type type = field.getGenericType();
          Class acturalType;
          if (type instanceof ParameterizedType) {
            acturalType = (Class) ((ParameterizedType) type)
              .getActualTypeArguments()[0];
          } else {
            throw new ThymeException(
              field.getName()
                + " 类型不对，必须是：ParameterizedType 类型, "
                + "例如：List<String>");
          }
          ReflectionUtils.setField(field, entity,
            Jsons.decodeList((String) val, acturalType));
        } else if (Map.class.isAssignableFrom(field.getType())
          && val instanceof String) {
          ReflectionUtils.setField(field, entity,
            Jsons.decode((String) val, field.getType()));
        } else {
          ReflectionUtils.setField(field, entity, val);
        }
      }
    });
    return (Entity) entity;
  }

  private <T> T convert(Number val, Class targetType) {
    if (val.getClass() == targetType) {
      return (T) val;
    } else if (targetType == Short.class || targetType == short.class) {
      return (T) (Number) val.shortValue();
    } else if (targetType == Integer.class || targetType == int.class) {
      return (T) (Number) val.intValue();
    } else if (targetType == Long.class || targetType == long.class) {
      return (T) (Number) val.longValue();
    } else if (targetType == Float.class || targetType == float.class) {
      return (T) (Number) val.floatValue();
    } else if (targetType == Double.class || targetType == double.class) {
      return (T) (Number) val.doubleValue();
    } else if (targetType == Boolean.class || targetType == boolean.class) {
      return (T) (val.intValue() == 0 ? Boolean.FALSE : Boolean.TRUE);
    } else if (targetType == Object.class) {
      return (T) (Number) val.longValue();
    } else {
      throw new UnsupportedOperationException("Unsupported type: " + val.getClass());
    }
  }

  private MysqlQuery from(YixiQuery query) {
    query.validate();
    typeConverters.convert(query.getFilter());
    Map<String, Object> queryDoc = query.getFilter();
    return new MysqlQuery(new MysqlFilter(new Document<>(queryDoc)))
      .limit(query.getLimit())
      .skip(query.getSkip())
      .fields(query.getFields())
      .sort(query.getSorts())
      .count(query.isCount());
  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  interface Func {

    void call(org.jooq.Field field, Object val);
  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  public static class QueryContext {

    private final MysqlQuery mysqlQuery;
    private final JooqClassMeta jooqClassMeta;
    private final Condition condition;
    private final List<SortField> sortFields;

    private ConditionParser conditionParser;

    public QueryContext(MysqlQuery mysqlQuery, JooqClassMeta jooqClassMeta) {
      this.mysqlQuery = mysqlQuery;
      this.jooqClassMeta = jooqClassMeta;
      this.conditionParser = new ConditionParser(jooqClassMeta, mysqlQuery.joins());
      condition = conditionParser.parseCondition(mysqlQuery.filter().doc());
      sortFields = parseSorts(mysqlQuery.sorts());
    }

    public Condition condition() {
      return condition;
    }

    public List<SortField> sorts() {
      return sortFields;
    }

    private List<SortField> parseSorts(Map<String, Integer> sorts) {
      if (sorts != null && !sorts.isEmpty()) {
        List<SortField> sortFields = new ArrayList<>();
        sorts.forEach((k, v) -> {
          TableField tableField = conditionParser.getTableField(k);
          if (v == -1) {
            sortFields.add(tableField.desc());
          } else {
            sortFields.add(tableField.asc());
          }
        });
        return sortFields;
      } else {
        return new ArrayList<>();
      }
    }
  }

  /**
   * @author yixi
   */
  public static class ConditionParser {

    private final JooqClassMeta jooqClassMeta;
    private final Map<String, JooqClassMeta> joinJooqClassMetas = new HashMap<>();

    public ConditionParser(JooqClassMeta jooqClassMeta) {
      this(jooqClassMeta, null);
    }

    public ConditionParser(JooqClassMeta jooqClassMeta, List<MysqlQuery.Join> joins) {
      this.jooqClassMeta = jooqClassMeta;
      if (joins != null) {
        joinJooqClassMetaMap("", joins, joinJooqClassMetas);
      }
    }

    public TableField getTableField(String key) {
      TableField tableField = jooqClassMeta.getTableField(key);
      if (tableField == null) {
        int i = key.lastIndexOf(".");
        if (i > 0) {
          String prefix = key.substring(0, i);
          JooqClassMeta classMeta = joinJooqClassMetas.get(prefix);
          if (classMeta != null) {
            tableField = classMeta.getTableField(key.substring(i + 1));
          }
        }
      }
      if (tableField == null) {
        throw new BusinessException("field not exist in table, field: " + key);
      }

      return tableField;
    }

    private Condition parseCondition(Map<String, Object> mysqlFilter) {
      Condition condition = null;
      Set<String> keys = mysqlFilter.keySet();
      for (String key : keys) {
        if ("$or".equals(key)) {
          Object val = mysqlFilter.get("$or");
          if (!(val instanceof List)) {
            throw new ThymeException("$or must be list. value: " + val);
          }
          Condition or = null;
          for (Object obj : (List) val) {
            if (!(obj instanceof Map)) {
              throw new ThymeException("$or item type must be Object. value: " + obj);
            }
            Condition next = parseCondition((Map) obj);
            or = or == null ? next : or.or(next);
          }
          condition = condition == null ? or : condition.and(or);
        } else if ("$and".equals(key)) {
          Object val = mysqlFilter.get("$and");
          if (!(val instanceof List)) {
            throw new ThymeException("$and must be list. value: " + val);
          }
          Condition and = null;
          for (Object obj : (List) val) {
            if (!(obj instanceof Map)) {
              throw new ThymeException(
                "$and item type must be Object. value: " + obj);
            }
            Condition next = parseCondition((Map) obj);
            and = and == null ? next : and.and(next);
          }
          condition = condition == null ? and : condition.and(and);
        } else {
          Object val = mysqlFilter.get(key);
          org.jooq.Field field = getTableField(key);
          if (val instanceof Map) {
            Map<String, Object> map = (Map) val;
            for (String op : map.keySet()) {
              Condition c;
              if ("$eq".equals(op)) {
                c = field.eq(conditionVal(map.get(op)));
              } else if ("$gt".equals(op)) {
                c = field.gt(conditionVal(map.get(op)));
              } else if ("$gte".equals(op)) {
                c = field.ge(conditionVal(map.get(op)));
              } else if ("$lt".equals(op)) {
                c = field.lt(conditionVal(map.get(op)));
              } else if ("$lte".equals(op)) {
                c = field.le(conditionVal(map.get(op)));
              } else if ("$ne".equals(op)) {
                c = field.ne(conditionVal(map.get(op)));
              } else if ("$in".equals(op)) {
                c = field.in((List) map.get(op));
              } else if ("$exists".equals(op)) {
                Object o = map.get(op);
                if (o instanceof Boolean) {
                  boolean b = (Boolean) o;
                  if (b) {
                    c = field.isNull();
                  } else {
                    c = field.isNotNull();
                  }
                } else {
                  throw new IllegalArgumentException(
                    "Invalid data type, must be boolean." + o.getClass()
                      .getSimpleName());
                }
              } else if ("$nin".equals(op)) {
                c = field.notIn(map.get(op));
              } else if ("$regex".equals(op) && map.get(op) != null) {
                c = field.likeRegex(map.get(op).toString());
              } else {
                throw new UnsupportedOperationException("Unsupported op: " + op);
              }
              condition = condition == null ? c : condition.and(c);
            }
          } else {
            condition =
              condition == null ? field.eq(conditionVal(val))
                : condition.and(field.eq(conditionVal(val)));
          }
        }
      }
      return condition;
    }

    public Object conditionVal(Object val) {
      if (val instanceof String) {
        String s = (String) val;
        if (s.startsWith("$ref:")) {
          return getTableField(s.substring(5));
        }
        return val;
      }
      return val;
    }

    public String parseFilter(Map<String, Object> mysqlFilter) {
      Set<String> keys = mysqlFilter.keySet();
      StringBuilder where = new StringBuilder();

      for (String key : keys) {
        if ("$or".equals(key)) {
          Object val = mysqlFilter.get("$or");
          if (!(val instanceof List)) {
            throw new ThymeException("$or must be list. value: " + val);
          }
          StringBuilder or = new StringBuilder();
          for (Object obj : (List) val) {
            if (!(obj instanceof Map)) {
              throw new ThymeException("$or item type must be Object. value: " + obj);
            }
            if (or.length() == 0) {
              or.append(parseFilter((Map) obj));
            } else {
              or.append(" OR ").append(parseFilter((Map) obj));
            }
          }
          if (where.length() == 0) {
            where.append("(").append(or.toString()).append(")");
          } else {
            where.append(" AND ").append("(").append(or.toString()).append(")");
          }
        } else if ("$and".equals(key)) {
          Object val = mysqlFilter.get("$and");
          if (!(val instanceof List)) {
            throw new ThymeException("$and must be list. value: " + val);
          }
          StringBuilder and = new StringBuilder();
          for (Object obj : (List) val) {
            if (!(obj instanceof Map)) {
              throw new ThymeException(
                "$and item type must be Object. value: " + obj);
            }
            if (and.length() == 0) {
              and.append(parseFilter((Map) obj));
            } else {
              and.append(" AND ").append(parseFilter((Map) obj));
            }
          }
          if (where.length() == 0) {
            where.append("(").append(and.toString()).append(")");
          } else {
            where.append(" AND ").append("(").append(and.toString()).append(")");
          }
        } else {
          Object val = mysqlFilter.get(key);
          org.jooq.Field field = getTableField(key);
          if (val instanceof Map) {
            Map<String, Object> map = (Map) val;
            for (String op : map.keySet()) {
              StringBuilder c = new StringBuilder();
              Object v = conditionVal(map.get(op));
              if ("$eq".equals(op)) {
                if (c.length() != 0) {
                  c.append(" AND ");
                }
                if (v == null) {
                  c.append(field.getName()).append(" is null ");
                } else {
                  c.append(field.getName()).append(" = ").append(convert(v));
                }
              } else if ("$gt".equals(op)) {
                if (c.length() != 0) {
                  c.append(" AND ");
                }
                c.append(field.getName()).append(" > ").append(convert(v));
              } else if ("$gte".equals(op)) {
                if (c.length() != 0) {
                  c.append(" AND ");
                }
                c.append(field.getName()).append(" >= ").append(convert(v));
              } else if ("$lt".equals(op)) {
                if (c.length() != 0) {
                  c.append(" AND ");
                }
                c.append(field.getName()).append(" < ").append(convert(v));
              } else if ("$lte".equals(op)) {
                if (c.length() != 0) {
                  c.append(" AND ");
                }
                c.append(field.getName()).append(" <= ").append(convert(v));
              } else if ("$ne".equals(op)) {
                if (c.length() != 0) {
                  c.append(" AND ");
                }
                if (v == null) {
                  c.append(field.getName()).append(" is not null ");
                } else {
                  c.append(field.getName()).append(" != ").append(convert(v));
                }
              } else if ("$in".equals(op)) {
                if (c.length() != 0) {
                  c.append(" AND ");
                }
                c.append(field.getName()).append(" in ").append(convertIn(v));
              } else if ("$nin".equals(op)) {
                if (c.length() != 0) {
                  c.append(" AND ");
                }
                c.append(field.getName()).append(" not in ").append(convertIn(v));
              } else {
                throw new UnsupportedOperationException("Unsupported op: " + op);
              }
              if (where.length() > 0) {
                where.append(" AND ");
              }
              where.append(c.toString());
            }
          } else {
            if (where.length() > 0) {
              where.append(" AND ");
            }
            if (val == null) {
              where.append(field.getName()).append(" is null ");
            } else {
              where.append(field.getName()).append(" = ").append(convert(val));
            }
          }
        }
      }
      return where.toString();
    }

    private void joinJooqClassMetaMap(String prefix, List<MysqlQuery.Join> joins,
      Map<String, JooqClassMeta> joinJooqClassMetaMap) {
      for (MysqlQuery.Join join : joins) {
        joinJooqClassMetaMap.put(prefix + join.ref(),
          JooqTables.classMetaRequire(join.clazz()));
        joinJooqClassMetaMap(join.ref() + ".", join.children(), joinJooqClassMetaMap);
      }
    }

    public String convertIn(Object v) {
      if (!(v instanceof List)) {
        throw new ThymeException("vlaue : " + v + ", must be array.");
      }
      StringBuilder sb = new StringBuilder();
      List list = (List) v;
      sb.append("(");
      for (int i = 0; i < list.size(); i++) {
        sb.append(convert(list.get(i)));
        if (i < list.size() - 1) {
          sb.append(",");
        }
      }
      sb.append(")");
      return sb.toString();
    }

    public Object convert(Object v) {
      if (v instanceof String) {
        return "'" + v.toString().replace("'", "\'") + "'";
      } else if (v instanceof Number) {
        return v;
      } else if (v instanceof Boolean) {
        return ((Boolean) v) ? 1 : 0;
      } else if (v instanceof Date) {
        return Thyme.DATE_FORMAT_GMT_8.format((Date) v);
      } else {
        throw new ThymeException("Unsupported data type: " + v);
      }
    }
  }

  /**
   * @author yixi
   */
  public static class Sql {

    private String table;

    public String getTable() {
      return table;
    }

    public void setTable(String table) {
      this.table = table;
    }
  }

  /**
   * @author yixi
   */
  @SuppressWarnings("all")
  public static class UpdateSql extends Sql {

    private Map<org.jooq.Field, Object> sets = new HashMap<>();
    private String filter;
    private Integer limit;

    public UpdateSql(String table, Map<org.jooq.Field, Object> sets, String filter,
      Integer limit) {
      setTable(table);
      this.sets = sets;
      this.filter = filter;
      this.limit = limit;
    }

    public Map<org.jooq.Field, Object> getSets() {
      return sets;
    }

    public void setSets(Map<org.jooq.Field, Object> sets) {
      this.sets = sets;
    }

    public String getFilter() {
      return filter;
    }

    public void setFilter(String filter) {
      this.filter = filter;
    }

    public Integer getLimit() {
      return limit;
    }

    public void setLimit(Integer limit) {
      this.limit = limit;
    }

    @Override
    public String toString() {
      if (sets.isEmpty()) {
        return null;
      }
      StringBuilder sql = new StringBuilder();
      sql.append("UPDATE ").append(getTable()).append(" SET ");
      int i = 0;
      for (Map.Entry<org.jooq.Field, Object> entry : sets.entrySet()) {
        sql.append("`").append(entry.getKey().getName()).append("`").append(" = ");
        Object v = entry.getValue();
        if (v instanceof String) {
          sql.append("'").append(v.toString().replace("'", "\'")).append("'");
        } else if (v instanceof Number) {
          sql.append(v.toString());
        } else if (v instanceof Boolean) {
          sql.append(((Boolean) v) ? 1 : 0);
        } else if (v instanceof Date) {
          sql.append("'")
            .append(Thyme.DATE_TIME_FORMAT_GMT_8.format((Date) v))
            .append("'");
        } else {
          throw new ThymeException("Unsupported data type: " + v);
        }
        i++;
        if (i != sets.size()) {
          sql.append(", ");
        }
      }
      if (StringUtils.isNotBlank(filter)) {
        sql.append(" WHERE ").append(filter);
      }
      if (limit > 0) {
        sql.append(" limit ").append(limit);
      }
      return sql.toString();
    }
  }
}
