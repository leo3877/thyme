package org.yixi.thyme.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author yixi
 * @since 1.0.0
 */
public abstract class Lists {

  public static <T, R> List<R> map(List<T> items, Function<T, R> function) {
    List<R> newItems = new ArrayList<>(items.size());
    for (T item : items) {
      newItems.add(function.apply(item));
    }
    return newItems;
  }
}
