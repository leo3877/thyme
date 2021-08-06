package org.yixi.thyme.core.worker;

import java.util.EventListener;

/**
 * The lifecycle interface for generic components.
 * <br>
 * Classes implementing this interface have a defined life cycle defined by the methods of this
 * interface.
 *
 * copy from jetty
 *
 * @author jetty
 */
interface LifeCycle {

  /**
   * Starts the component.
   *
   * @throws Exception If the component fails to start
   * @see #stop()
   * @see #isFailed()
   */
  void start() throws Exception;

  /**
   * Stops the component. The component may wait for current activities to complete normally, but it
   * can be interrupted.
   *
   * @throws Exception If the component fails to stop
   * @see #isStopped()
   * @see #start()
   * @see #isFailed()
   */
  void stop() throws Exception;

  void down() throws Exception;

  /**
   * @return true if the component is starting or has been started.
   */
  boolean isRunning();

  /**
   * @return true if the component is starting.
   */
  boolean isStarting();

  /**
   * @return true if the component is stopping.
   * @see #isStopped()
   */
  boolean isStopping();


  /**
   * @return true if the component has been stopped.
   * @see #stop()
   * @see #isStopping()
   */
  boolean isStopped();

  Status getState();

  /**
   * @return true if the component has failed to start or has failed to stop.
   */
  boolean isFailed();

  void addLifeCycleListener(Listener listener);

  void removeLifeCycleListener(Listener listener);


  /**
   * Listener. A listener for Lifecycle events.
   */
  interface Listener extends EventListener {

    void lifeCycleStarting(LifeCycle event);

    void lifeCycleStarted(LifeCycle event);

    void lifeCycleFailure(LifeCycle event, Throwable cause);

    void lifeCycleDowned(LifeCycle event);

    void lifeCycleStopping(LifeCycle event);

    void lifeCycleStopped(LifeCycle event);
  }


  /**
   * Utility to start an object if it is a LifeCycle and to convert any exception thrown to a {@link
   * RuntimeException}
   *
   * @param object The instance to start.
   * @throws RuntimeException if the call to start throws an exception.
   */
  static void start(Object object) {
    if (object instanceof LifeCycle) {
      try {
        ((LifeCycle) object).start();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Utility to stop an object if it is a LifeCycle and to convert any exception thrown to a {@link
   * RuntimeException}
   *
   * @param object The instance to stop.
   * @throws RuntimeException if the call to stop throws an exception.
   */
  static void stop(Object object) {
    if (object instanceof LifeCycle) {
      try {
        ((LifeCycle) object).stop();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
}
