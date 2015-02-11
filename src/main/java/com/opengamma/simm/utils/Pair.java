/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.simm.utils;

/**
 * An immutable pair consisting of two elements.
 * <p>
 * This implementation refers to the elements as 'first' and 'second'.
 * The elements cannot be null.
 *
 * @param <A> the type of the first element
 * @param <B> the type of the second element
 */
public final class Pair<A, B> {

  /**
   * The first element in this pair.
   */
  private final A first;

  /**
   * The second element in this pair.
   */
  private final B second;

  /**
   * Obtains a pair inferring the types.
   *
   * @param <A>    the first element type
   * @param <B>    the second element type
   * @param first  the first element
   * @param second the second element
   * @return a pair formed from the two parameters
   */
  public static <A, B> Pair<A, B> of(A first, B second) {
    return new Pair<>(first, second);
  }

  private Pair(A first, B second) {
    this.first = first;
    this.second = second;
  }

  /**
   * Get the first element of the pair.
   *
   * @return the first element
   */
  public A getFirst() {
    return first;
  }

  /**
   * Get the second element of the pair.
   *
   * @return the second element
   */
  public B getSecond() {
    return second;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Pair pair = (Pair) o;
    return first.equals(pair.first) && second.equals(pair.second);
  }

  @Override
  public int hashCode() {
    return 31 * first.hashCode() + second.hashCode();
  }

  @Override
  public String toString() {
    return "<" + first + "," + second + ">";
  }
}