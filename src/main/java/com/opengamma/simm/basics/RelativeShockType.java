/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.simm.basics;

/**
 * A relative shock type. This means that when a shock is applied to an
 * initial value, the result is generally the multiplication of the two.
 * However, a shift can be applied that modifies this calculation such that
 * it becomes: {@code return (initial + shift) * shock - shift;}
 */
public class RelativeShockType implements ShockType {

  /**
   * Constant for relative shocks with shift of zero.
   */
  private static final RelativeShockType ZERO_SHIFT_INSTANCE = new RelativeShockType(0);

  /**
   * The shift on the shock. May be zero
   */
  private final double shift;

  /**
   * Create a new relative shock.
   *
   * @param shift  the shift on the shock
   * @return a new relative shock
   */
  public static RelativeShockType of(double shift) {
    return new RelativeShockType(shift);
  }

  /**
   * Create a relative shock with no shift.
   *
   * @return a new relative shock
   */
  public static RelativeShockType of() {
    return ZERO_SHIFT_INSTANCE;
  }

  @Override
  public double calculateShiftedValue(double initial, double shock) {
    return (initial + shift) * shock - shift;
  }

  // Private constructor
  private RelativeShockType(double shift) {
    this.shift = shift;
  }
}
