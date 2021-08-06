package org.yixi.thyme.core.worker;

import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;

/**
 * Copy from jetty
 *
 * @author jetty
 * @author yixi
 */
@Slf4j
public abstract class AbstractLifeCycle implements LifeCycle {

  private final CopyOnWriteArrayList<Listener> _listeners = new CopyOnWriteArrayList<>();

  private final Object _lock = new Object();

  private volatile Status state = Status.Runnable;

  protected void doStart() throws Exception {
  }

  protected void doStop() throws Exception {
  }

  @Override
  public final void start() throws Exception {
    synchronized (_lock) {
      try {
        if (state == Status.Starting || state == Status.Running) {
          return;
        }
        setStarting();
        doStart();
        setStarted();
      } catch (Throwable e) {
        setFailed(e);
        throw e;
      }
    }
    log.info("[Worker] start successful, {}", this);
  }

  @Override
  public final void stop() throws Exception {
    synchronized (_lock) {
      try {
        if (state == Status.Stopping || state == Status.Stopped) {
          return;
        }
        setStopping();
        doStop();
        setStopped();
      } catch (Throwable e) {
        setFailed(e);
        throw e;
      }
      log.info("[Worker] stop successful, {}", this);
    }
  }

  @Override
  public void down() throws Exception {
    synchronized (_lock) {
      try {
        if (state == Status.Downed) {
          return;
        }
        setStopping();
        doStop();
        setDowned();
      } catch (Throwable e) {
        setFailed(e);
        throw e;
      }
      log.info("[Worker] down successful, {}", this);
    }
  }

  @Override
  public boolean isRunning() {
    return state == Status.Running;
  }

  @Override
  public boolean isStarting() {
    return state == Status.Starting;
  }

  @Override
  public boolean isStopping() {
    return state == Status.Stopping;
  }

  @Override
  public boolean isStopped() {
    return state == Status.Stopped;
  }

  @Override
  public boolean isFailed() {
    return state == Status.Failed;
  }

  @Override
  public void addLifeCycleListener(Listener listener) {
    _listeners.add(listener);
  }

  @Override
  public void removeLifeCycleListener(Listener listener) {
    _listeners.remove(listener);
  }

  @Override
  public Status getState() {
    return state;
  }

  private void setStarted() {
    state = Status.Running;
    if (log.isDebugEnabled()) {
      log.debug(state + " {}", this);
    }
    for (Listener listener : _listeners) {
      listener.lifeCycleStarted(this);
    }
  }

  private void setStarting() {
    if (log.isDebugEnabled()) {
      log.debug("starting {}", this);
    }
    state = Status.Starting;
    for (Listener listener : _listeners) {
      listener.lifeCycleStarting(this);
    }
  }

  private void setStopping() {
    if (log.isDebugEnabled()) {
      log.debug("stopping {}", this);
    }
    state = Status.Stopping;
    for (Listener listener : _listeners) {
      listener.lifeCycleStopping(this);
    }
  }

  private void setStopped() {
    state = Status.Stopped;
    if (log.isDebugEnabled()) {
      log.debug("{} {}", state, this);
    }
    for (Listener listener : _listeners) {
      listener.lifeCycleStopped(this);
    }
  }

  private void setDowned() {
    state = Status.Downed;
    if (log.isDebugEnabled()) {
      log.debug("{} {}", state, this);
    }
    for (Listener listener : _listeners) {
      listener.lifeCycleDowned(this);
    }
  }

  protected void setFailed(Throwable th) {
    state = Status.Failed;
    log.warn(state + " " + this + ": " + th, th);
    for (Listener listener : _listeners) {
      listener.lifeCycleFailure(this, th);
    }
  }

  public static abstract class AbstractLifeCycleListener implements Listener {

    @Override
    public void lifeCycleFailure(LifeCycle event, Throwable cause) {
    }

    @Override
    public void lifeCycleStarted(LifeCycle event) {
    }

    @Override
    public void lifeCycleStarting(LifeCycle event) {
    }

    @Override
    public void lifeCycleStopped(LifeCycle event) {
    }

    @Override
    public void lifeCycleStopping(LifeCycle event) {
    }

    @Override
    public void lifeCycleDowned(LifeCycle event) {
    }
  }

  public static final Listener STOP_ON_FAILURE = new AbstractLifeCycleListener() {
    @Override
    public void lifeCycleFailure(LifeCycle lifecycle, Throwable cause) {
      try {
        lifecycle.stop();
      } catch (Exception e) {
        cause.addSuppressed(e);
      }
    }
  };
}
