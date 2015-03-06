/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.opensimm.basics;

/**
 * A shock definition to be applied to some market data. This class
 * specifies how a shock should be applied, not the shock itself (which
 * is just a double value).
 */
public interface ShockType {

  /**
   * Calculate the effect of a shock on an initial value using this
   * shock definition.
   *
   * @param initial  the value to be shocked
   * @param shock  the shock to be applied
   * @return the result of applying the shock to the value
   */
  public abstract double calculateShiftedValue(double initial, double shock);
}
