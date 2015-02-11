/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.simm.basics;

/**
 * An absolute shock type. This means that when a shock is applied to an
 * initial value, the result is the addition of the two.
 */
public class AbsoluteShockType implements ShockType {

  /**
   * Static instance can be used for all clients.
   */
  private static final AbsoluteShockType INSTANCE = new AbsoluteShockType();

  /**
   * Create an absolute shock.
   *
   * @return an absolute shock
   */
  public static AbsoluteShockType of() {
    return INSTANCE;
  }

  // Private constructor to prevent instantiation
  private AbsoluteShockType() {
  }

  @Override
  public double calculateShiftedValue(double initial, double shock) {
    return initial + shock;
  }
}
