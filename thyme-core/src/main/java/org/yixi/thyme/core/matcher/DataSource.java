package org.yixi.thyme.core.matcher;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author yixi
 */
public class DataSource {

  private final Map<String, Object> datum = new HashMap<>();

  public DataSource() {

  }

  public DataSource(Map datum) {
    this.datum.putAll(datum);
  }

  public static DataSource from(Map datum) {
    return new DataSource(datum);
  }

  public DataSource addValue(String key, Object value) {
    datum.put(key, value);
    return this;
  }

  public DataSource addValues(Map values) {
    datum.putAll(values);
    return this;
  }

  public DataSource addFunc(Supplier supplier, String... keys) {
    for (String key : keys) {
      datum.put(key, new SupplierContext(supplier));
    }
    return this;
  }

  public DataSource remove(String key) {
    datum.remove(key);
    return this;
  }

  Object call(String key) {
    Object obj = datum.get(key);
    if (obj == null) {
      return null;
    } else if (obj instanceof SupplierContext) {
      Object res = ((SupplierContext) obj).call();
      if (res instanceof Map) {
        datum.putAll((Map) res);
      } else {
        datum.put(key, res);
      }
      return datum.get(key);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * @author yixi
   */
  public static class SupplierContext {

    private boolean exec;
    private final Supplier supplier;

    public SupplierContext(Supplier supplier) {
      this.supplier = supplier;
    }

    public Object call() {
      if (exec) {
        return null;
      } else {
        exec = true;
        return supplier.get();
      }
    }

    public boolean isExec() {
      return exec;
    }
  }
}
