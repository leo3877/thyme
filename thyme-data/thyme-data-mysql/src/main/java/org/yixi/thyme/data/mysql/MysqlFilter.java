package org.yixi.thyme.data.mysql;

import java.util.Map;
import java.util.regex.Pattern;
import org.yixi.thyme.data.BaseQuery;
import org.yixi.thyme.data.QueryOperators;

/**
 * @author yixi
 * @since 1.0.1
 */
public class MysqlFilter extends BaseQuery.Filter<MysqlFilter> {

  public MysqlFilter() {
    super();
  }

  public MysqlFilter(Map<String, Object> filter) {
    super(filter);
  }

  public static MysqlFilter create() {
    return new MysqlFilter();
  }

  public static MysqlFilter create(Map<String, Object> filter) {
    return new MysqlFilter(filter);
  }

  @Override
  public MysqlFilter regex(Pattern regex) {
    addOperand(QueryOperators.REGEX, regex);
    return this;
  }

  @Override
  public MysqlFilter copy() {
    MysqlFilter copy = create(filters);
    copy.currentKey = this.currentKey;
    copy.hasNot = this.hasNot;
    return copy;
  }
}
