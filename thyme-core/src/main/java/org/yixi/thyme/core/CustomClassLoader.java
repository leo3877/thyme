package org.yixi.thyme.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yixi
 * @since 1.0.0
 */
public class CustomClassLoader extends ClassLoader {

  private static final Logger logger = LoggerFactory.getLogger(CustomClassLoader.class);

  private ClassLoader parentClassLoader;

  public CustomClassLoader() {
    this(CustomClassLoader.class.getClassLoader());
  }

  public CustomClassLoader(ClassLoader parentClassLoader) {
    this.parentClassLoader = parentClassLoader;
  }

  @Override
  public Class<?> loadClass(String className) {
    Class<?> clazz = findLoadedClass(className);
    if (clazz == null) {
      try {
        clazz = parentClassLoader.loadClass(className);
      } catch (ClassNotFoundException e) {
        // ignore
        logger.warn("ClassNotFound: " + className);
      }
    }

    return clazz;
  }
}
