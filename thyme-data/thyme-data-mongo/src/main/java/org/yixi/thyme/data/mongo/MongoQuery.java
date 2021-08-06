package org.yixi.thyme.data.mongo;

import java.util.List;
import java.util.Map;
import org.yixi.thyme.core.Document;
import org.yixi.thyme.data.BaseQuery;

/**
 * Mongo 查询类。
 *
 * @author yixi
 * @since 1.0.0
 */
public class MongoQuery extends BaseQuery<MongoQuery, MongoFilter> {

  private boolean explain;

  public MongoQuery() {
    this(new MongoFilter());
  }

  public MongoQuery(MongoFilter filter) {
    super(filter);
  }

  public static MongoQuery create() {
    return new MongoQuery();
  }

  public static MongoQuery create(MongoFilter mongoFilter) {
    return new MongoQuery(mongoFilter);
  }

  public static MongoQuery create(Map<String, Object> filter) {
    MongoQuery mongoQuery = MongoQuery.create();
    mongoQuery.filter = MongoFilter.create(filter);
    return mongoQuery;
  }

  @Override
  public MongoQuery copy() {
    MongoQuery query = MongoQuery.create(filter);
    query.limit = limit;
    query.skip = skip;
    query.fields = fields;
    query.count = count;
    query.sorts = sorts;
    return query;
  }

  @Override
  public MongoQuery sort(String key, OrderType orderType) {
    if ("id".equals(key)) {
      super.sort("_id", orderType);
    } else {
      super.sort(key, orderType);
    }
    return this;
  }

  @Override
  public MongoQuery sort(Map<String, Integer> sorts) {
    Integer id = sorts.get("id");
    if (id != null) {
      sorts.remove("id");
      sorts.put("_id", id);
    }
    super.sort(sorts);
    return this;
  }

  public MongoQuery mod(final Object object) {
    filter.mod(object);
    return this;
  }

  public MongoQuery all(final Object object) {
    filter.all(object);
    return this;
  }

  public MongoQuery size(final Object object) {
    filter.size(object);
    return this;
  }

  public MongoQuery elemMatch(final Document match) {
    filter.elemMatch(match);
    return this;
  }

  public MongoQuery explain() {
    explain = true;
    return this;
  }

  public boolean isExplain() {
    return explain;
  }

  /**
   * Equivalent of the $within operand, used for geospatial operation
   */
  public MongoQuery withinCenter(final double x, final double y, final double radius) {
    filter.withinCenter(x, y, radius);
    return this;
  }

  public MongoQuery near(final double x, final double y) {
    filter.near(x, y);
    return this;
  }

  public MongoQuery near(final double x, final double y, final double maxDistance) {
    filter.near(x, y, maxDistance);
    return this;
  }

  /**
   * Equivalent of the $nearSphere operand
   */
  public MongoQuery nearSphere(final double longitude, final double latitude) {
    filter.nearSphere(longitude, latitude);
    return this;
  }

  public MongoQuery nearSphere(final double longitude, final double latitude,
    final double minDistance,
    final double maxDistance) {
    filter.nearSphere(longitude, latitude, minDistance, maxDistance);
    return this;
  }

  /**
   * Equivalent of the $centerSphere operand mostly intended for queries up to a few hundred miles
   * or km.
   */
  public MongoQuery withinCenterSphere(final double longitude, final double latitude,
    final double maxDistance) {
    filter.withinCenterSphere(longitude, latitude, maxDistance);
    return this;
  }

  /**
   * Equivalent to a $within operand, based on a bounding box using represented by two corners
   */
  public MongoQuery withinBox(final double x, final double y, final double x2,
    final double y2) {
    filter.withinBox(x, y, x2, y2);
    return this;
  }

  /**
   * Equivalent to a $within operand, based on a bounding polygon represented by an array of points
   */
  public MongoQuery withinPolygon(final List<Double[]> points) {
    filter.withinPolygon(points);
    return this;
  }

  /**
   * Equivalent to a $text operand.
   */
  public MongoQuery text(final String search) {
    return text(search, null);
  }

  /**
   * Equivalent to a $text operand.
   */
  public MongoQuery text(final String search, final String language) {
    filter.text(search, language);
    return this;
  }

  /**
   * Equivalent to $not meta operator. Must be followed by an operand, not a value, e.g. {@code
   * MongoQuery.instance().key("val").not().mod(Arrays.asList(10, 1)) }
   */
  public MongoQuery not() {
    filter.not();
    return this;
  }

  @Override
  public String toString() {
    return "MongoQuery{" +
      "filter=" + filter +
      ", limit=" + limit +
      ", skip=" + skip +
      ", count=" + count +
      ", sorts=" + sorts +
      ", fields=" + fields +
      '}';
  }
}
