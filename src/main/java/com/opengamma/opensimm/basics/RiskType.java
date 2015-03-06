/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.opensimm.basics;

import java.util.stream.Stream;

/**
 * The risk type for a risk factor.
 * <p>
 * The risk type will determine how the shocks are
 * applied to the base level.
 */
public enum RiskType {
  /**
   * Sensitivity-base risk type.
   */
  SENSITIVITY("SE"),
  /**
   * Exposure-based risk type.
   */
  EXPOSURE("EX");

  private final String shortCode;

  /**
   * Parse the risk type from the supplied string.
   * <p>
   * An attempt is made to find a matching risk type
   * using either the full name or the defined short code.
   * The match is case-insensitive.
   *
   * @param riskType  the risk type name to parse
   * @return the {@code RiskType} matching the supplied string,
   *   otherwise an exception is thrown
   * @throws IllegalArgumentException if the no matching asset class can be found
   */
  public static RiskType parse(String riskType) {

    String upper = riskType.toUpperCase();
    return Stream.of(values())
        .filter(ac -> ac.name().equals(upper) || ac.shortCode.equals(upper))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unable to parse a risk type from: " + riskType));
  }

  // private constructor
  private RiskType(String shortCode) {
    this.shortCode = shortCode;
  }
}
