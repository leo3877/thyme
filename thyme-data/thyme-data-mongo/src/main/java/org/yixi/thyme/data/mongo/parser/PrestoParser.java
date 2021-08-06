package org.yixi.thyme.data.mongo.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;
import org.yixi.data.client.RestFilter;
import org.yixi.data.client.YixiQuery;
import org.yixi.data.client.YixiQuery.Builder;
import org.yixi.thyme.core.Thyme;

/**
 * @author yixi
 * @since 1.0.0
 */
public class PrestoParser implements Parser<String> {

  public static void main(String[] args) {
    Parser<String> parser = new PrestoParser();
    YixiQuery query = Builder.builder()
      .key("test").exists(null)
      .key("m").gt(1).lt(10)
      .or(RestFilter.create().key("aaa").gt(11), RestFilter.create().key("sss").eq("dadada"))
      .key("regex").startsWith("hello world")
      .key("not_regex").not().contains("88")
      .build();
    String string = parser.parse(query);
    System.out.println(string);
  }

  @Override
  public String parse(YixiQuery query) {
    String where = parseSql(query.getFilter());
    String select = parseSelect(query.getFields());
    return where;
  }

  public String parseSelect(Map<String, Boolean> fields) {
    Collection<Boolean> values = fields.values();
    if (values.size() > 0 && values.iterator().next()) {
      StringBuilder sb = new StringBuilder();
      int i = 0;
      for (String key : fields.keySet()) {
        sb.append(key);
        if (++i < fields.size()) {
          sb.append(", ");
        }
      }
      return sb.toString();
    }
    return "*";
  }

  public String parseSql(Map<String, Object> filter) {
    StringBuilder sb = new StringBuilder();
    int j = 0;
    for (Entry<String, Object> entry : filter.entrySet()) {
      String k = entry.getKey();
      Object v = entry.getValue();
      if (k.equals("__and")) {
        List<String> ands = parse((List) v);
        if (!ands.isEmpty()) {
          sb.append("(");
          int s = 0;
          for (String childFilter : ands) {
            sb.append(childFilter);
            if (++s < ands.size()) {
              sb.append(" add ");
            }
          }
          sb.append(")");
        }
      } else if (k.equals("__or")) {
        List<String> ors = parse((List) v);
        if (!ors.isEmpty()) {
          sb.append("(");
          int s = 0;
          for (String childFilter : ors) {
            sb.append(childFilter);
            if (++s < ors.size()) {
              sb.append(" or ");
            }
          }
          sb.append(")");
        }
      } else if (v instanceof Map) {
        Map<String, Object> m = (Map) v;
        Object notMap = m.get("__not");
        boolean not = false;
        if (notMap != null) {
          not = true;
          if (notMap instanceof Map) {
            m = (Map) notMap;
          } else {
            m = null;
            sb.append(k).append(" = ").append(sqlValue(notMap));
          }
        }
        if (m != null && !m.isEmpty()) {
          int n = 0;
          for (Entry<String, Object> operators : m.entrySet()) {
            String k1 = operators.getKey();
            Object v1 = operators.getValue();
            if (k1.equals("eq")) {
              not(sb, not);
              sb.append(k).append(" = ").append(sqlValue(v1));
            } else if (k1.equals("ne")) {
              not(sb, not);
              sb.append(k).append(" != ").append(sqlValue(v1));
            } else if (k1.equals("gt")) {
              not(sb, not);
              sb.append(k).append(" > ").append(sqlValue(v1));
            } else if (k1.equals("gte")) {
              not(sb, not);
              sb.append(k).append(" >= ").append(sqlValue(v1));
            } else if (k1.equals("lt")) {
              not(sb, not);
              sb.append(k).append(" < ").append(sqlValue(v1));
            } else if (k1.equals("lte")) {
              not(sb, not);
              sb.append(k).append(" <= ").append(sqlValue(v1));
            } else if (k1.equals("in")) {
              not(sb, not);
              sb.append(k).append(" in ");
              if (v1 instanceof List) {
                sb.append("(");
                for (int i = 0; i < ((List) v1).size(); i++) {
                  sb.append(sqlValue(((List) v1).get(i)));
                  if (i < ((List) v1).size() - 1) {
                    sb.append(",");
                  }
                }
                sb.append(")");
              } else {
                throw Thyme.ex("Syntax invalid. %s must List", v1.toString());
              }
            } else if (k1.equals("nin")) {
              not(sb, not);
              sb.append(k).append(" not in ");
              if (v1 instanceof List) {
                sb.append("(");
                for (int i = 0; i < ((List) v1).size(); i++) {
                  sb.append(sqlValue(((List) v1).get(i)));
                  if (i < ((List) v1).size() - 1) {
                    sb.append(",");
                  }
                }
                sb.append(")");
              } else {
                throw Thyme.ex("Syntax invalid. %s must List", v1.toString());
              }
            } else if (k1.equals("exists")) {
              not(sb, not);
              if (v1 instanceof Boolean && (Boolean) v1) {
                sb.append(k).append(" is not null");
              } else {
                sb.append(k).append(" is null");
              }
            } else if (k1.equals("regex")) {
              not(sb, not);
              sb.append("regexp_like(").append(k).append(",").append(sqlValue(v1)).append(")");
            } else if (k1.equals("contains")) {
              if (StringUtils.isNotBlank((String) v1)) {
                not(sb, not);
                sb.append("regexp_like(").append(k).append(",").append(sqlValue(v1)).append(")");
              }
            } else if (k1.equals("startsWith")) {
              if (StringUtils.isNotBlank((String) v1)) {
                not(sb, not);
                sb.append("regexp_like(").append(k).append(",").append(sqlValue("^" + v1))
                  .append(")");
              }
            } else if (k1.equals("endsWith")) {
              if (StringUtils.isNotBlank((String) v1)) {
                not(sb, not);
                sb.append("regexp_like(").append(k).append(",").append(sqlValue(v1 + "$"))
                  .append(")");
              }
            } else if (k1.equals("allOf")) {
              // TODO
            } else if (k1.equals("notOf")) {
              // TODO
            } else if (k1.equals("anyOf")) {
              // TODO
            } else {
              throw Thyme
                .exUnsupported("Syntax invalid, Unsupported Operator: %s in Type: Presto", k1);
            }
            if (++n < m.size()) {
              sb.append(" and ");
            }
          }
        }
      } else {
        sb.append(k).append(" = ").append(sqlValue(v));
      }
      if (++j < filter.size()) {
        sb.append(" and ");
      }
    }
    return sb.toString();
  }

  private void not(StringBuilder sb, boolean not) {
    if (not) {
      sb.append(" not ");
    }
  }

  private List<String> parse(List<Map> filters) {
    List<String> sqls = new ArrayList<>();
    for (Map filter : filters) {
      sqls.add(parseSql(filter));
    }
    return sqls;
  }

  Object sqlValue(Object val) {
    if (val instanceof String) {
      return "'" + val + "'";
    } else if (val instanceof Date) {
      return "timestamp '" + Thyme.DATE_TIME_FORMAT_GMT_8.format((Date) val) + "'";
    } else {
      return val;
    }
  }
}
