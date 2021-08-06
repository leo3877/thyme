package org.yixi.thyme.data.graphdata;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yixi
 * @since 1.0.0
 */
public class GraphQueryThreadPoolExecutor implements ExecutorService {

  private static final Logger logger = LoggerFactory.getLogger(
    GraphQueryThreadPoolExecutor.class);

  private static final int DEFAULT_MAX_THREAD_SIZE = 500;

  private ExecutorService executorService;
  private int maxThreadSize;

  public GraphQueryThreadPoolExecutor() {
    this(0);
  }

  public GraphQueryThreadPoolExecutor(int maxThreadSize) {
    if (maxThreadSize > 0) {
      this.maxThreadSize = maxThreadSize;
    } else {
      this.maxThreadSize = DEFAULT_MAX_THREAD_SIZE;
    }
    executorService = new ThreadPoolExecutor(
      5, // com.wacai.creditcard.core size
      this.maxThreadSize, // max pool size
      200, // keep alive time when thread idle
      TimeUnit.MINUTES,
      new ArrayBlockingQueue<>(10000),
      new GraphDataThreadFactory(),
      (r, executor) -> {
        logger.warn("Server busy now!!");
        throw new RejectedExecutionException("Server busy now!!");
      });
  }

  @Override
  public void shutdown() {
    executorService.shutdown();
  }

  @Override
  public List<Runnable> shutdownNow() {
    return executorService.shutdownNow();
  }

  @Override
  public boolean isShutdown() {
    return executorService.isShutdown();
  }

  @Override
  public boolean isTerminated() {
    return executorService.isTerminated();
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    return executorService.awaitTermination(timeout, unit);
  }

  @Override
  public <T> Future<T> submit(Callable<T> task) {
    return executorService.submit(task);
  }

  @Override
  public <T> Future<T> submit(Runnable task, T result) {
    return executorService.submit(task, result);
  }

  @Override
  public Future<?> submit(Runnable task) {
    return executorService.submit(task);
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
    throws InterruptedException {
    return executorService.invokeAll(tasks);
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout,
    TimeUnit unit) throws InterruptedException {
    return executorService.invokeAll(tasks, timeout, unit);
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
    throws InterruptedException, ExecutionException {
    return executorService.invokeAny(tasks);
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
    throws InterruptedException, ExecutionException, TimeoutException {
    return executorService.invokeAny(tasks, timeout, unit);
  }

  @Override
  public void execute(Runnable command) {
    executorService.execute(command);
  }

  private static class GraphDataThreadFactory implements ThreadFactory {

    private AtomicInteger count = new AtomicInteger(0);

    @Override
    public Thread newThread(Runnable r) {
      Thread t = new Thread(r);
      t.setName("GraphQueryThreadPoolExecutor-" + count.addAndGet(1));
      return t;
    }
  }
}
