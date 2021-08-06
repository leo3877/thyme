package org.yixi.thyme.data.mongo.tail;

import java.util.Map;
import org.bson.BsonTimestamp;
import org.bson.Document;

/**
 * @author yixi
 * @since 1.2.1
 */
@SuppressWarnings("all")
public class MongoOplog extends OplogTailer.Oplog {

  private BsonTimestamp ts; // 操作发生时间, 是一个带版本的时间戳
  private Long t; // version
  private Long h; // a hash (signed Long), 无需关注
  private Integer v; // version, 无需关注
  private OplogTailer.Op op; // 操作类型
  private String ns; // 操作所在的库和表, 格式：database.table, eg：finance_data.blank_list
  private Map<String, Object> o; // 当 op == i, 即插入内容;  op == u, 即更新内容; op == d, 即删除条件
  private Map<String, Object> o2; // 当 op == u， 即更新条件

  private Document raw; // mongo 原始 oplog

  public BsonTimestamp getTs() {
    return ts;
  }

  public void setTs(BsonTimestamp ts) {
    this.ts = ts;
  }

  public Long getT() {
    return t;
  }

  public void setT(Long t) {
    this.t = t;
  }

  public Long getH() {
    return h;
  }

  public void setH(Long h) {
    this.h = h;
  }

  public Integer getV() {
    return v;
  }

  public void setV(Integer v) {
    this.v = v;
  }

  public OplogTailer.Op getOp() {
    return op;
  }

  public void setOp(OplogTailer.Op op) {
    this.op = op;
  }

  public String getNs() {
    return ns;
  }

  public void setNs(String ns) {
    this.ns = ns;
  }

  public Map<String, Object> getO() {
    return o;
  }

  public void setO(Map<String, Object> o) {
    this.o = o;
  }

  public Map<String, Object> getO2() {
    return o2;
  }

  public void setO2(Map<String, Object> o2) {
    this.o2 = o2;
  }

  public Document getRaw() {
    return raw;
  }

  void setRaw(Document raw) {
    this.raw = raw;
  }

  @Override
  public String toString() {
    return "MongoOplog{" +
      "ts=" + ts.getTime() +
      ", t=" + t +
      ", h=" + h +
      ", v=" + v +
      ", op=" + op +
      ", ns='" + ns + '\'' +
      ", o=" + o +
      ", o2=" + o2 +
      '}';
  }
}
