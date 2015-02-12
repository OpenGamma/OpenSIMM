/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.simm.util;

import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;

/**
 * Utility methods for creating unmodifiable Maps and Lists.
 * Ideally, we would use Guava Immutable collections.
 */
public class CollectionUtils {

  public static <K, V> Map<K, V> createMap(K k1, V v1) {
    Map<K, V> m = new HashMap<>();
    m.put(k1, v1);
    return Collections.unmodifiableMap(m);
  }

  public static <K, V> Map<K, V> createMap(K k1, V v1, K k2, V v2) {
    Map<K, V> m = new HashMap<>();
    m.put(k1, v1);
    m.put(k2, v2);
    return Collections.unmodifiableMap(m);
  }

  public static <K, V> Map<K, V> createMap(K k1, V v1, K k2, V v2, K k3, V v3) {
    Map<K, V> m = new HashMap<>();
    m.put(k1, v1);
    m.put(k2, v2);
    m.put(k3, v3);
    return Collections.unmodifiableMap(m);
  }

  public static <K, V> Map<K, V> createMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
    Map<K, V> m = new HashMap<>();
    m.put(k1, v1);
    m.put(k2, v2);
    m.put(k3, v3);
    m.put(k4, v4);
    return Collections.unmodifiableMap(m);
  }

  public static <K, V> Map<K, V> createMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
    Map<K, V> m = new HashMap<>();
    m.put(k1, v1);
    m.put(k2, v2);
    m.put(k3, v3);
    m.put(k4, v4);
    m.put(k5, v5);
    return Collections.unmodifiableMap(m);
  }

  public static <K, V> Map<K, V> createMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
    Map<K, V> m = new HashMap<>();
    m.put(k1, v1);
    m.put(k2, v2);
    m.put(k3, v3);
    m.put(k4, v4);
    m.put(k5, v5);
    m.put(k6, v6);
    return Collections.unmodifiableMap(m);
  }

  public static <K, V> Map<K, V> createMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7) {
    Map<K, V> m = new HashMap<>();
    m.put(k1, v1);
    m.put(k2, v2);
    m.put(k3, v3);
    m.put(k4, v4);
    m.put(k5, v5);
    m.put(k6, v6);
    m.put(k7, v7);
    return Collections.unmodifiableMap(m);
  }

  @SafeVarargs
  public static <T> List<T> createList(T... items) {
    return Arrays.asList(items);
  }

  public static <K, V> Collector<Pair<K, V>, ?, Map<K, V>> pairsToMap() {
    return toMap(Pair::getFirst, Pair::getSecond);
  }
  public static <K, V> Collector<Map.Entry<K, V>, ?, Map<K, V>> entriesToMap() {
    return toMap(Map.Entry::getKey, Map.Entry::getValue);
  }
}
