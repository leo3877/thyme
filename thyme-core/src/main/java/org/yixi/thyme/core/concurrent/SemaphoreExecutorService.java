package org.yixi.thyme.core.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yixi
 * @since 1.0.0
 */
public class SemaphoreExecutorService implements ExecutorService {

  private final ExecutorService executor;
  private final Semaphore available;

  public SemaphoreExecutorService(String executorName, int size) {
    executor = new ThreadPoolExecutor(size, size, 5, TimeUnit.SECONDS,
      new ArrayBlockingQueue<>(size),
      new ThreadFactory() {
        final AtomicInteger count = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
          return new Thread(r, executorName + count.incrementAndGet());
        }
      });
    available = new Semaphore(size, true);
  }

  @Override
  public void execute(Runnable command) {
    try {
      available.acquire();
    } catch (InterruptedException e) {
      // ignore
    }

    executor.execute(() -> {
      try {
        command.run();
      } finally {
        available.release();
      }
    });
  }


  @Override
  public void shutdown() {
    executor.shutdown();
  }

  @Override
  public List<Runnable> shutdownNow() {
    return executor.shutdownNow();
  }

  @Override
  public boolean isShutdown() {
    return executor.isShutdown();
  }

  @Override
  public boolean isTerminated() {
    return executor.isTerminated();
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    return executor.awaitTermination(timeout, unit);
  }

  @Override
  public <T> Future<T> submit(Callable<T> task) {
    try {
      available.acquire();
    } catch (InterruptedException e) {
      // ignore
    }
    return executor.submit(new WrapperCallable<>(task, available));
  }

  @Override
  public <T> Future<T> submit(Runnable task, T result) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Future<?> submit(Runnable task) {
    try {
      available.acquire();
    } catch (InterruptedException e) {
      // ignore
    }

    return executor.submit(() -> {
      try {
        task.run();
      } finally {
        available.release();
      }
    });
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
    throws InterruptedException {
    List<Callable<T>> callables = new ArrayList<>();
    for (Callable<T> task : tasks) {
      callables.add(new WrapperCallable<>(task, available));
    }
    return executor.invokeAll(wrapperTasks(tasks));
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout,
    TimeUnit unit) throws InterruptedException {

    return executor.invokeAll(wrapperTasks(tasks), timeout, unit);
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
    throws InterruptedException, ExecutionException {
    return executor.invokeAny(wrapperTasks(tasks));
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
    throws InterruptedException, ExecutionException, TimeoutException {
    return executor.invokeAny(wrapperTasks(tasks), timeout, unit);
  }

  private <T> List<Callable<T>> wrapperTasks(Collection<? extends Callable<T>> tasks) {
    List<Callable<T>> callables = new ArrayList<>();
    for (Callable<T> task : tasks) {
      callables.add(new WrapperCallable<>(task, available));
    }
    return callables;
  }

  /**
   * @author yixi
   */
  public static class WrapperCallable<V> implements Callable<V> {

    private final Callable<V> callable;
    private final Semaphore available;

    public WrapperCallable(Callable<V> callable, Semaphore available) {
      this.callable = callable;
      this.available = available;
    }

    @Override
    public V call() throws Exception {
      try {
        return callable.call();
      } finally {
        available.release();
      }
    }
  }
}
