/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.simm.basics;

import com.opengamma.simm.util.ArgChecker;

/**
 * A standard risk factor. A risk factor wraps a user
 * defined name, and as such is unconstrained in the
 * values it can contain.
 */
public class StandardRiskFactor implements RiskFactor {

  private final String name;

  /**
   * Create a risk factor with the specified name.
   *
   * @param name  the name for the risk factor
   * @return a new {@code RiskFactor} instance
   */
  public static StandardRiskFactor of(String name) {
    return new StandardRiskFactor(name);
  }

  private StandardRiskFactor(String name) {
    this.name = ArgChecker.notNull(name, "name");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    StandardRiskFactor that = (StandardRiskFactor) o;
    return name.equals(that.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public String toString() {
    return "Non FX risk factor (" + name + ")";
  }
}
