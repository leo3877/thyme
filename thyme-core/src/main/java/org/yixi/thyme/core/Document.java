/*
 * Copyright 2008-2015 MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.yixi.thyme.core;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 从 Mongo driver 移植而来
 *
 * @author mongo
 * @since 1.0.0
 */
public class Document<K, V> implements Map<K, V>, Serializable {

  private static final long serialVersionUID = -5773525004864316886L;

  private final LinkedHashMap<K, V> doc;

  public Document() {
    doc = new LinkedHashMap<>();
  }

  public Document(final K key, final V value) {
    doc = new LinkedHashMap<>();
    doc.put(key, value);
  }

  public Document(final Map<K, V> map) {
    doc = new LinkedHashMap<>(map);
  }

  public Document append(final K key, final V value) {
    doc.put(key, value);
    return this;
  }

  public Integer getInteger(final Object key) {
    return (Integer) get(key);
  }

  public int getInteger(final Object key, final int defaultValue) {
    Object value = get(key);
    return value == null ? defaultValue : (Integer) value;
  }

  public Long getLong(final Object key) {
    return (Long) get(key);
  }

  public Double getDouble(final Object key) {
    return (Double) get(key);
  }

  public String getString(final Object key) {
    return (String) get(key);
  }

  public Boolean getBoolean(final Object key) {
    return (Boolean) get(key);
  }

  public boolean getBoolean(final Object key, final boolean defaultValue) {
    Object value = get(key);
    return value == null ? defaultValue : (Boolean) value;
  }

  public Date getDate(final Object key) {
    return (Date) get(key);
  }

  @Override
  public int size() {
    return doc.size();
  }

  @Override
  public boolean isEmpty() {
    return doc.isEmpty();
  }

  @Override
  public boolean containsValue(final Object value) {
    return doc.containsValue(value);
  }

  @Override
  public boolean containsKey(final Object key) {
    return doc.containsKey(key);
  }

  @Override
  public V get(final Object key) {
    return doc.get(key);
  }

  public <T> T get(final Object key, final Class<T> clazz) {
    return clazz.cast(doc.get(key));
  }

  @Override
  public V put(final K key, final V value) {
    return doc.put(key, value);
  }

  @Override
  public V remove(final Object key) {
    return doc.remove(key);
  }

  @Override
  public void putAll(final Map map) {
    doc.putAll(map);
  }

  @Override
  public void clear() {
    doc.clear();
  }

  @Override
  public Set<K> keySet() {
    return doc.keySet();
  }

  @Override
  public Collection<V> values() {
    return doc.values();
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    return doc.entrySet();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Document document = (Document) o;
    return doc.equals(document.doc);
  }

  @Override
  public int hashCode() {
    return doc.hashCode();
  }

  @Override
  public String toString() {
    return doc.toString();
  }
}
