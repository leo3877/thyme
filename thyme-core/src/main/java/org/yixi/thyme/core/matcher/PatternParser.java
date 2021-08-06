package org.yixi.thyme.core.matcher;

import java.util.List;
import java.util.Map;

/**
 * @author yixi
 */
public class PatternParser {

    public static Pattern.Combine fromDsl(Map<String, Object> dsl) {
        return fromDsl(dsl, false);
    }

    public static Pattern.Combine fromDsl(Map<String, Object> dsl, boolean ignoreNull) {
        Pattern.Combine combine = new Pattern.Combine();
        dsl.forEach((k, v) -> {
            if (Operator.__and.name().equals(k)
                || Operator.__or.name().equals(k)
                || Operator.__not.name().equals(k)) {
                if (v instanceof List) {
                    combine.add(k, parseLogic(k, (List) v, ignoreNull));
                } else {
                    throw new IllegalArgumentException(
                        "error type: must be List, type: " + v.getClass().getSimpleName());
                }
            } else if (v instanceof Map) {
                Map map = (Map) v;
                if (map.size() > 1) {
                    String type = (String) map.get("__type");
                    if (type != null) {
                        map.remove("__type");
                        if (map.size() > 1) {
                            Map.Entry one = (Map.Entry) map.entrySet().iterator().next();
                            combine.add(k, new Pattern.Custom(type,
                                fromValue((String) one.getKey(), one.getValue(), ignoreNull)));
                        } else {
                            Pattern.ArrayValue arrayValue = new Pattern.ArrayValue();
                            map.forEach((k1, v1) -> arrayValue.add(new Pattern.Custom(type,
                                fromValue(k1.toString(), v1, ignoreNull))));
                            combine.add(k, arrayValue);
                        }
                    } else {
                        Pattern.ArrayValue arrayValue = new Pattern.ArrayValue();
                        map.forEach(
                            (k1, v1) -> arrayValue.add(fromValue(k1.toString(), v1, ignoreNull)));
                        combine.add(k, arrayValue);
                    }
                } else if (map.size() == 1) {
                    Map.Entry one = (Map.Entry) map.entrySet().iterator().next();
                    combine.add(k, fromValue((String) one.getKey(), one.getValue(), ignoreNull));
                }
            } else {
                combine.add(k, fromValue("eq", v, ignoreNull));
            }
        });
        return combine;
    }

    public static Pattern.Value fromValue(Operator operator, Object val) {
        return fromValue(operator, val, false);
    }

    public static Pattern.Value fromValue(String operator, Object val) {
        return fromValue(operator, val, false);
    }

    public static Pattern.Value fromValue(String operator, Object val, boolean ignoreNull) {
        try {
            return fromValue(Operator.valueOf(operator), val, ignoreNull);
        } catch (Exception e) {
            throw new UnsupportedOperationException("Unsupported operator: " + operator);
        }
    }

    public static Pattern.Value fromValue(Operator op, Object val, boolean ignoreNull) {
        if (op == Operator.eq) {
            return new Pattern.Eq(val, ignoreNull);
        } else if (op == Operator.ne) {
            return new Pattern.Ne(val, ignoreNull);
        } else if (op == Operator.gt) {
            return new Pattern.Gt(val, ignoreNull);
        } else if (op == Operator.gte) {
            return new Pattern.Gte(val, ignoreNull);
        } else if (op == Operator.lt) {
            return new Pattern.Lt(val, ignoreNull);
        } else if (op == Operator.lte) {
            return new Pattern.Lte(val, ignoreNull);
        } else if (op == Operator.in) {
            return new Pattern.In(val, ignoreNull);
        } else if (op == Operator.nin) {
            return new Pattern.Nin(val, ignoreNull);
        } else if (op == Operator.anyOf) {
            return new Pattern.AnyOf(val, ignoreNull);
        } else if (op == Operator.allOf) {
            return new Pattern.AllOf(val, ignoreNull);
        } else if (op == Operator.notOf) {
            return new Pattern.NotOf(val, ignoreNull);
        } else if (op == Operator.contains) {
            return new Pattern.Contains(val, ignoreNull);
        } else if (op == Operator.regex) {
            return new Pattern.Regex(val, ignoreNull);
        } else if (op == Operator.startsWith) {
            return new Pattern.StartsWith(val, ignoreNull);
        } else if (op == Operator.endsWith) {
            return new Pattern.EndsWith(val, ignoreNull);
        } else if (op == Operator.antPath) {
            return new Pattern.AntPath(val, ignoreNull);
        } else if (op == Operator.exists) {
            return new Pattern.Exists(val, ignoreNull);
        } else {
            throw new UnsupportedOperationException("Unsupported value operator: " + op);
        }
    }

    private static Pattern.Logic parseLogic(String operator, List<Object> dsl, boolean ignoreNull) {
        Operator op;
        try {
            op = Operator.valueOf(operator);
        } catch (Exception e) {
            throw new UnsupportedOperationException("Unsupported operator: " + operator);
        }
        Pattern.Logic logic;
        if (op == Operator.__and) {
            logic = new Pattern.And();
        } else if (op == Operator.__or) {
            logic = new Pattern.Or();
        } else if (op == Operator.__not) {
            logic = new Pattern.Not();
        } else {
            throw new UnsupportedOperationException("Unsupported logic operator: " + op);
        }
        for (Object obj : dsl) {
            if (obj instanceof Map) {
                logic.add(fromDsl((Map) obj, ignoreNull));
            } else {
                throw new IllegalArgumentException(
                    "error type: must be Map, type: " + obj.getClass().getSimpleName());
            }
        }
        return logic;
    }
}
