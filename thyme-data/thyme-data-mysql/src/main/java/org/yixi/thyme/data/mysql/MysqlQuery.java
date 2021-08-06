package org.yixi.thyme.data.mysql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jooq.TableField;
import org.yixi.thyme.core.Document;
import org.yixi.thyme.data.BaseQuery;

/**
 * Mysql 查询类。
 *
 * @author yixi
 * @since 1.0.1
 */
public class MysqlQuery extends BaseQuery<MysqlQuery, MysqlFilter> {

  private List<Join> joins = new ArrayList<>();
  private boolean selectForUpdate;

  public MysqlQuery() {
    super(new MysqlFilter());
  }

  public MysqlQuery(MysqlFilter filter) {
    super(filter);
  }

  public static MysqlQuery create() {
    return new MysqlQuery();
  }

  public static MysqlQuery create(MysqlFilter filter) {
    return new MysqlQuery(filter);
  }

  public static MysqlQuery create(Map<String, Object> filter) {
    MysqlQuery mysqlQuery = new MysqlQuery();
    mysqlQuery.filter = MysqlFilter.create(filter);
    return mysqlQuery;
  }

  @Override
  public MysqlQuery copy() {
    MysqlQuery query = MysqlQuery.create(filter);
    query.limit = limit;
    query.skip = skip;
    query.count = count;
    if (fields != null) {
      query.fields = new Document<>(fields);
    }
    if (sorts != null) {
      query.sorts = new Document<>(sorts);
    }
    query.selectForUpdate = selectForUpdate;
    if (!this.joins.isEmpty()) {
      for (Join join : this.joins) {
        query.joins.add(join.copy());
      }
    }
    return query;
  }

  public Join join(String field) {
    Join join = new Join(field);
    joins.add(join);
    return join;
  }

  public List<Join> joins() {
    return joins;
  }

  public MysqlQuery setSelectForUpdate(boolean selectForUpdate) {
    this.selectForUpdate = selectForUpdate;
    return this;
  }

  public boolean isSelectForUpdate() {
    return selectForUpdate;
  }

  /**
   * @author yixi
   * @since 1.0.1
   */
  public static class Join {

    protected String ref; // 关联父对象的那个字段
    protected MysqlFilter on = new JoinMysqlFilter(this); // join 条件
    protected Document<String, Boolean> fields = new Document<>(); // 返回那些字段
    protected JoinType joinType; // join 类型
    protected List<Join> children = new ArrayList<>(); // 继续 join 子表
    protected Class clazz; // join 那张表

    private Map<String, TableField> tableFieldMap;

    public Join(String ref) {
      this(ref, JoinType.INNER, new MysqlFilter());
    }

    public Join(String ref, JoinType joinType) {
      this(ref, joinType, new MysqlFilter());
    }

    public Join(String ref, JoinType joinType, MysqlFilter on) {
      this.ref = ref;
      this.joinType = joinType;
      this.on = on;
    }

    public String ref() {
      return ref;
    }

    public Join clazz(Class clazz) {
      this.clazz = clazz;
      return this;
    }

    public Class clazz() {
      return clazz;
    }

    public Join field(String field, Boolean show) {
      this.fields.put(field, show);
      return this;
    }

    public Join fields(Document<String, Boolean> fields) {
      if (fields != null) {
        this.fields.putAll(fields);
      }
      return this;
    }

    public Document<String, Boolean> fields() {
      return fields;
    }

    public Join on(String field, String refField) {
      this.on.key(field).eq(refField);
      return this;
    }

    public MysqlFilter on() {
      return on;
    }

    public Join using(String field) {
      this.on.key(field).eq("$ref:" + ref() + "." + field);
      return this;
    }

    public List<Join> children() {
      return children;
    }

    public JoinType joinType() {
      return joinType;
    }

    public Join join(String ref) {
      Join join = new Join(ref);
      children.add(join);
      return join;
    }

    public Join join(String ref, JoinType joinType) {
      Join join = new Join(ref, joinType);
      children.add(join);
      return join;
    }

    public Join join(String ref, JoinType joinType, MysqlFilter on) {
      Join join = new Join(ref, joinType, on);
      children.add(join);
      return join;
    }

    public Join copy() {
      Join join = new Join(this.ref);
      join.fields(this.fields);
      join.on = this.on.copy();
      join.clazz = this.clazz;
      join.joinType = this.joinType;
      for (Join j : this.children) {
        join.children.add(j.copy());
      }
      return join;
    }

    void setTableFieldMap(Map<String, TableField> tableFieldMap) {
      this.tableFieldMap = tableFieldMap;
    }

    Map<String, TableField> getTableFieldMap() {
      return tableFieldMap;
    }
  }

  /**
   * @author yixi
   */
  public static class JoinMysqlFilter extends MysqlFilter {

    private Join join;

    JoinMysqlFilter(Join join) {
      this.join = join;
    }

    public Join join() {
      return join;
    }
  }

  /**
   * @author yixi
   */
  public enum JoinType {
    INNER,

    LEFT,

    RIGHT,

    OUTER
  }
}
