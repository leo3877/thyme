package org.yixi.thyme.data.mongo.tail;

import java.util.function.Consumer;
import org.bson.BsonTimestamp;

/**
 * @author yixi
 * @since 1.2.0
 */
public class LeaderOplogTailer<T extends OplogTailer.Oplog> implements OplogTailer<T> {

  private final OplogTailer<T> tailer;

  public LeaderOplogTailer(OplogTailer<T> tailer) {
    this.tailer = tailer;
  }


  @Override
  public void setCheckPoint(BsonTimestamp checkPoint) {
    tailer.setCheckPoint(checkPoint);
  }

  @Override
  public void start() {
    tailer.start();
  }

  @Override
  public void stop() {
    tailer.stop();
  }

  @Override
  public Integer status() {
    return tailer.status();
  }

  @Override
  public BsonTimestamp currentCheckPoint() {
    return tailer.currentCheckPoint();
  }

  @Override
  public void addOplogListener(Consumer fn) {
    tailer.addOplogListener(fn);
  }

  @Override
  public void addOplogListener(String database, Consumer fn) {
    tailer.addOplogListener(database, fn);
  }

  @Override
  public void addOplogListener(String database, String table, Consumer fn) {
    tailer.addOplogListener(database, table, fn);
  }

  @Override
  public void removeOplogListener(Consumer<T> fn) {
    tailer.removeOplogListener(fn);
  }

  @Override
  public void removeOplogListener(String database, Consumer<T> fn) {
    tailer.removeOplogListener(database, fn);
  }

  @Override
  public void removeOplogListener(String database, String table, Consumer<T> fn) {
    tailer.removeOplogListener(database, table, fn);
  }

  @Override
  public void whenStop(Consumer fn) {
    tailer.whenStop(fn);
  }

  @Override
  public void whenStart(Consumer fn) {
    tailer.whenStart(fn);
  }
}
