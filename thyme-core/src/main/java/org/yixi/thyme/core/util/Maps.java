package org.yixi.thyme.core.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapDifference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author yixi
 * @since 1.0.0
 */
public abstract class Maps {

  public static <K, V> Map<K, V> newHashMap() {
    return com.google.common.collect.Maps.newHashMap();
  }

  public static <K, V> Map<K, V> newHashMap(Map<? extends K, ? extends V> map) {
    return com.google.common.collect.Maps.newHashMap(map);
  }

  public static <K, V> Map<K, V> newEmptyMap() {
    return Collections.emptyMap();
  }

  public static <K, V> Map<K, V> newSingletonMap(K k, V v) {
    return Collections.singletonMap(k, v);
  }

  public static <K, V> ImmutableMap<K, V> newImmutableMap(Map<K, V> map) {
    return ImmutableMap.copyOf(map);
  }

  public static <K, V> ArrayListMultimap<K, V> newArrayListMultimap() {
    return ArrayListMultimap.create();
  }

  public static <K, V> HashMultimap<K, V> newSetMultimap() {
    return HashMultimap.create();
  }

  public static <K, V> Map<K, V> synchronizedMap(Map<K, V> map) {
    return Collections.synchronizedMap(map);
  }

  public static <K, V> Map<K, V> synchronizedMap(SortedMap<K, V> map) {
    return Collections.synchronizedSortedMap(map);
  }

  public static <K, V> LinkedHashMap<K, V> newLinkedHashMap() {
    return com.google.common.collect.Maps.newLinkedHashMap();
  }

  public static <K, V> LinkedHashMap<K, V> newLinkedHashMap(Map<? extends K, ? extends V> map) {
    return com.google.common.collect.Maps.newLinkedHashMap(map);
  }

  public static <K, V> MapDifference<K, V> difference(Map<? extends K, ? extends V> left,
    Map<? extends K, ? extends V> right) {
    return com.google.common.collect.Maps.difference(left, right);
  }

  public static <K, V1, V2> Map<K, V2> transformValues(Map<K, V1> fromMap,
    com.google.common.base.Function<? super V1, V2> function) {
    return com.google.common.collect.Maps.transformValues(fromMap, function);
  }

  public static <O, K, V> Map<K, V> toHashMap(Collection<O> values,
    Function<O, K> keyMapper,
    Function<O, V> valueMapper) {
    Map<K, V> map = new LinkedHashMap<>();
    return toMap(values, keyMapper, valueMapper, new HashMap<>());
  }

  public static <O, K, V> Map<K, V> toLinkedHashMap(Collection<O> values,
    Function<O, K> keyMapper,
    Function<O, V> valueMapper) {
    return toMap(values, keyMapper, valueMapper, new LinkedHashMap<>());
  }

  public static <O, K, V> Map<K, V> toMap(Collection<O> values,
    Function<O, K> keyMapper,
    Function<O, V> valueMapper,
    Map<K, V> map) {
    for (O o : values) {
      map.put(keyMapper.apply(o), valueMapper.apply(o));
    }
    return map;
  }

  public static <K, V> Map.Entry<K, V> findAnyMatch(Map<K, V> map,
    Predicate<Entry<K, V>> predicate) {
    if (map == null) {
      return null;
    }
    for (Map.Entry<K, V> entry : map.entrySet()) {
      if (predicate.test(entry)) {
        return entry;
      }
    }
    return null;
  }

  public static <L, R> Map<String, R> mergeToRight(Map<String, L> left, Map<String, R> right,
    MergeFunction<L, R> mergeFunction) {
    left.forEach((k, v) -> right.put(k, mergeFunction.merge(k, v, right.get(k))));
    return right;
  }

  /**
   * @author yixi
   */
  public interface MergeFunction<L, R> {

    R merge(String key, L l, R r);
  }
}
