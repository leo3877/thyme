package org.yixi.thyme.data.mongo;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.yixi.thyme.core.Document;
import org.yixi.thyme.data.BaseQuery;
import org.yixi.thyme.data.QueryOperators;

/**
 * @author yixi
 * @since 1.0.1
 */
public class MongoFilter extends BaseQuery.Filter<MongoFilter> {

  public MongoFilter() {
    super();
  }

  public MongoFilter(Map<String, Object> filter) {
    super(filter);
  }

  public static MongoFilter create() {
    return new MongoFilter();
  }

  public static MongoFilter create(Map<String, Object> filter) {
    return new MongoFilter(filter);
  }

  @Override
  public MongoFilter copy() {
    return new MongoFilter(filters);
  }

  @Override
  public MongoFilter key(final String key) {
    if ("id".equals(key)) {
      super.key("_id");
    } else {
      super.key(key);
    }
    return this;
  }

  public MongoFilter mod(final Object object) {
    addOperand(QueryOperators.MOD, object);
    return this;
  }

  public MongoFilter all(final Object object) {
    addOperand(QueryOperators.ALL, object);
    return this;
  }

  public MongoFilter size(final Object object) {
    addOperand(QueryOperators.SIZE, object);
    return this;
  }

  public MongoFilter exists(final Object object) {
    addOperand(QueryOperators.EXISTS, object);
    return this;
  }

  public MongoFilter elemMatch(final Document match) {
    addOperand(QueryOperators.ELEM_MATCH, match);
    return this;
  }

  public MongoFilter withinCenter(final double x, final double y, final double radius) {
    addOperand(QueryOperators.WITHIN,
      new Document<>(QueryOperators.CENTER,
        Lists.newArrayList(Lists.newArrayList(x, y), radius)));
    return this;
  }

  public MongoFilter near(final double x, final double y) {
    addOperand(QueryOperators.NEAR, Lists.newArrayList(x, y));
    return this;
  }

  public MongoFilter near(final double x, final double y, final double maxDistance) {
    addOperand(QueryOperators.NEAR, Lists.newArrayList(x, y));
    addOperand(QueryOperators.MAX_DISTANCE, maxDistance);
    return this;
  }

  public MongoFilter nearSphere(final double longitude, final double latitude) {
    addOperand(QueryOperators.NEAR_SPHERE, Lists.newArrayList(longitude, latitude));
    return this;
  }

  public MongoFilter nearSphere(final double longitude, final double latitude,
    final double minDistance,
    final double maxDistance) {
    addOperand(QueryOperators.NEAR_SPHERE, new Document("$geometry", new Document("type", "Point")
      .append("coordinates", Lists.newArrayList(longitude, latitude)))
      .append(QueryOperators.MIN_DISTANCE, minDistance)
      .append(QueryOperators.MAX_DISTANCE, maxDistance));
    return this;
  }

  public MongoFilter withinCenterSphere(final double longitude, final double latitude,
    final double maxDistance) {
    addOperand(QueryOperators.WITHIN, new Document<>(QueryOperators.CENTER_SPHERE,
      Lists.newArrayList(Lists.newArrayList(longitude, latitude), maxDistance)));
    return this;
  }

  public MongoFilter withinBox(final double x, final double y, final double x2,
    final double y2) {
    addOperand(QueryOperators.WITHIN, new Document<>(QueryOperators.BOX,
      new Object[]{new Double[]{x, y}, new Double[]{x2, y2}}));
    return this;
  }

  public MongoFilter withinPolygon(final List<Double[]> points) {
    if (points == null || points.isEmpty() || points.size() < 3) {
      throw new IllegalArgumentException(
        "Polygon insufficient number of vertices defined");
    }

    addOperand(QueryOperators.WITHIN,
      new Document<>(QueryOperators.POLYGON, convertToListOfLists(points)));
    return this;
  }

  public MongoFilter text(final String search) {
    return text(search, null);
  }

  public MongoFilter text(final String search, final String language) {
    if (currentKey != null) {
      throw new BaseQuery.QueryException(
        "The text operand may only occur at the top-level of a query. It does"
          + " not apply to a specific element, but rather to a document as a "
          + "whole.");
    }

    key(QueryOperators.TEXT);
    addOperand(QueryOperators.SEARCH, search);
    if (language != null) {
      addOperand(QueryOperators.LANGUAGE, language);
    }

    return this;
  }

  /**
   * Equivalent to $not meta operator. Must be followed by an operand, not a value, e.g. {@code
   * MongoQuery.instance().key("val").not().mod(Arrays.asList(10, 1)) }
   */
  public MongoFilter not() {
    hasNot = true;
    return this;
  }

  private List<List<Double>> convertToListOfLists(final List<Double[]> points) {
    List<List<Double>> listOfLists = new ArrayList<>(points.size());
    for (Double[] cur : points) {
      List<Double> list = new ArrayList<>(cur.length);
      Collections.addAll(list, cur);
      listOfLists.add(list);
    }
    return listOfLists;
  }

  @Override
  public String toString() {
    return filters.toString();
  }
}
