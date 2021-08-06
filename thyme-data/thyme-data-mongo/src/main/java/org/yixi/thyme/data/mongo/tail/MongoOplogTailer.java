package org.yixi.thyme.data.mongo.tail;

import com.mongodb.CursorType;
import com.mongodb.ReadPreference;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCursor;
import org.yixi.thyme.core.Retryer;
import org.yixi.thyme.core.Thyme;
import org.yixi.thyme.core.ex.RetryableException;
import org.yixi.thyme.core.ex.ThymeException;
import org.yixi.thyme.core.json.Jsons;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.bson.BsonTimestamp;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yixi
 * @since 1.2.1
 */
@SuppressWarnings("all")
public class MongoOplogTailer implements OplogTailer<MongoOplog> {

  private static final Logger logger = LoggerFactory.getLogger(MongoOplogTailer.class);

  private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
    Thread thread = new Thread(r);
    thread.setName("OplogTailer executor");
    return thread;
  });

  /**
   * 以 5 秒以前的时间作为 checkPoint 初始化时间
   */
  private final AtomicReference<BsonTimestamp> checkPoint = new AtomicReference<>(
    new BsonTimestamp((int) (System.currentTimeMillis() / 1000) - 5, 0));

  private final AtomicReference<Integer> status = new AtomicReference<>(0);

  private final ConcurrentMap<String, List<Consumer<MongoOplog>>> oplogListeners = new
    ConcurrentHashMap<>();
  private final ConcurrentMap<String, List<Consumer<MongoOplog>>> failureOplogListeners = new
    ConcurrentHashMap<>();
  private final List<Consumer<BsonTimestamp>> whenStarts = new ArrayList<>();
  private final List<Consumer<BsonTimestamp>> whenStops = new ArrayList<>();

  private final MongoClient mongoClient;

  private MongoCursor<Document> cursor;

  public MongoOplogTailer(MongoClient mongoClient) {
    this.mongoClient = mongoClient;
  }

  @Override
  public void setCheckPoint(BsonTimestamp checkPoint) {
    if (status.get() == 1) {
      throw new ThymeException("Tailer 正在运行中, 无法修改 checkpoint. 如需设置请先停止.");
    }
    this.checkPoint.set(checkPoint);
  }

  @Override
  public synchronized void start() {
    if (status.get() == 1) {
      logger.warn("Tailer 已经在运行中, 请勿重复启动");
      return;
    }
    whenStarts.forEach(fn -> {
      try {
        fn.accept(currentCheckPoint());
      } catch (Exception e) {
        logger.warn("When exe start fn({}) error: {}", fn.getClass().getName(),
          e.getMessage());
      }
    });
    tail();
    logger.info("Start tailer successful. ts: " + Jsons.encode(
      new Date(currentCheckPoint().getTime() * 1000L)));
  }

  @Override
  public synchronized void stop() {
    if (cursor != null) {
      cursor.close();
    }
    whenStops.forEach(fn -> {
      try {
        fn.accept(currentCheckPoint());
      } catch (Exception e) {
        logger.warn("When exe stop fn({}) error: {}", fn.getClass().getName(), e.getMessage());
      }
    });
    logger.info("Stop tailer successful. ts: " + Jsons.encode(
      new Date(currentCheckPoint().getTime() * 1000L)));
  }

  @Override
  public BsonTimestamp currentCheckPoint() {
    return checkPoint.get();
  }

  @Override
  public synchronized void addOplogListener(Consumer<MongoOplog> fn) {
    add("__all", fn);
  }

  @Override
  public synchronized void addOplogListener(String database, Consumer<MongoOplog> fn) {
    add(database, fn);
  }

  @Override
  public synchronized void addOplogListener(String database, String table,
    Consumer<MongoOplog> fn) {
    add(database + "." + table, fn);
  }

  @Override
  public synchronized void removeOplogListener(Consumer<MongoOplog> fn) {
    remove("__all", fn);
  }

  @Override
  public synchronized void removeOplogListener(String database, Consumer<MongoOplog> fn) {
    remove(database, fn);
  }

  @Override
  public synchronized void removeOplogListener(String database, String table,
    Consumer<MongoOplog> fn) {
    remove(database + "." + table, fn);
  }

  @Override
  public void whenStart(Consumer<BsonTimestamp> fn) {
    whenStarts.add(fn);
  }

  @Override
  public void whenStop(Consumer<BsonTimestamp> fn) {
    whenStops.add(fn);
  }

  @Override
  public Integer status() {
    return status.get();
  }

  private void tail() {
    // 发生重试异常则无限重试，每一轮重试间隔默认 2000 ms 到最大 5 分钟，每轮 20 次
    executor.execute(() -> Thyme.retryForever(new Retryer.Default(2000, 5 * 60 * 1000, 7), () -> {
      tail0();
      return null;
    }));
  }

  private void tail0() {
    status.set(1);
    cursor = mongoClient.getDatabase("local")
      .getCollection("oplog.rs")
      // .withReadConcern(ReadConcern.MAJORITY) // 保证分布式集群数据不会回滚
      .withReadPreference(ReadPreference.secondaryPreferred()) // 优先 tail 集群从节点
      .find(new Document().append("ts", new Document("$gte", checkPoint.get())))
      .batchSize(5000)
      .cursorType(CursorType.TailableAwait)
      .maxAwaitTime(3 * 24 * 60 * 60, TimeUnit.SECONDS)
      .iterator();

    try {
      cursor.forEachRemaining(this::emit);
    } catch (Exception e) {
      if (e instanceof IllegalStateException && e.getMessage().contains("Cursor has been closed")) {
        status.set(2);
        throw e; // 主动关闭 tail 不再重试
      } else {
        throw new RetryableException(e.getMessage(), e); // 出现服务器端异常或者 IO 异常则 tail 重试
      }
    }
  }

  private synchronized void emit(Document doc) {
    MongoOplog mongoOplog = toMongoOplog(doc);
    if (mongoOplog != null) {
      List<Consumer<MongoOplog>> all = oplogListeners.get("__all");
      if (all != null) {
        internalCall(mongoOplog, "__all", all);
      }
      String ns = mongoOplog.getNs();
      int i = ns.indexOf(".");
      if (i > 0) {
        String database = ns.substring(0, i);
        List<Consumer<MongoOplog>> databases = oplogListeners.get(database);
        if (databases != null) {
          internalCall(mongoOplog, database, databases);
        }
      }
      List<Consumer<MongoOplog>> tables = oplogListeners.get(mongoOplog.getNs());
      if (tables != null) {
        internalCall(mongoOplog, mongoOplog.getNs(), tables);
      }
      refreshCheckPoint(mongoOplog.getTs());
    }
  }

  private void internalCall(MongoOplog oplog, String key, List<Consumer<MongoOplog>> fns) {
    boolean needClean = false;
    for (int i = 0; i < fns.size(); i++) {
      try {
        fns.get(i).accept(oplog);
      } catch (Exception e) { // 当函数调用出错的时候从调用列表移除
        logger.warn("call fn({}) error: {}, ts: {}", fns.get(i).getClass().getName(),
          e.getMessage(),
          Jsons.encode(new Date(currentCheckPoint().getTime() * 1000L)));
        List<Consumer<MongoOplog>> consumers = failureOplogListeners.get(key);
        if (consumers == null) {
          consumers = new ArrayList<>();
          failureOplogListeners.putIfAbsent(key, consumers);
        }
        consumers.add(fns.get(i));
        needClean = true;
      }
    }
    if (needClean) {
      failureOplogListeners.get(key).forEach(fns::remove);
    }
  }

  private MongoOplog toMongoOplog(Document doc) {
    try {
      MongoOplog oplog = new MongoOplog();
      oplog.setH((Long) doc.get("h"));
      oplog.setNs((String) doc.get("ns"));
      oplog.setO((Map) doc.get("o"));
      oplog.setO2((Map) doc.get("o2"));
      oplog.setOp(OplogTailer.Op.valueOf(doc.get("op").toString()));
      oplog.setT((Long) doc.get("t"));
      oplog.setTs((BsonTimestamp) doc.get("ts"));
      oplog.setV((Integer) doc.get("v"));
      oplog.setRaw(doc);
      return oplog;
    } catch (Exception e) {
      logger.warn("Build MongoOplog error: {}", e.getMessage());
      // ignore
    }
    return null;
  }

  private void refreshCheckPoint(BsonTimestamp ts) {
    checkPoint.set(ts);
  }

  private void remove(String key, Consumer<MongoOplog> fn) {
    List<Consumer<MongoOplog>> consumers = oplogListeners.get(key);
    if (consumers != null) {
      consumers.remove(fn);
    }
  }

  private void add(String key, Consumer<MongoOplog> fn) {
    List<Consumer<MongoOplog>> fns = oplogListeners.get(key);
    if (fns == null) {
      fns = new ArrayList<>();
      oplogListeners.put(key, fns);
    }
    fns.add(fn);
  }
}

