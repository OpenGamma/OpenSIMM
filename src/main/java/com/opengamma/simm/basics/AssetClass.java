/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.simm.basics;

import java.util.stream.Stream;

/**
 * Enum holding the available asset classes for use
 * in the SIMM calculation.
 */
public enum AssetClass {
  /**
   * Commodity asset class.
   */
  COMMODITY("CO"),
  /**
   * Credit asset class.
   */
  CREDIT("CR"),
  /**
   * Equity asset class.
   */
  EQUITY("EQ"),
  /**
   * Interest rate asset class.
   */
  INTEREST_RATE("IR");

  private final String shortCode;

  // Private constructor for enum
  private AssetClass(String shortCode) {
    this.shortCode = shortCode;
  }

  /**
   * Parse the asset class from the supplied string.
   * <p>
   * An attempt is made to find a matching asset class
   * using either the full name or the defined short code.
   * The match is case-insensitive.
   *
   * @param assetClass  the asset class name to parse
   * @return the {@code AssetClass} matching the supplied string,
   *   otherwise an exception is thrown
   * @throws IllegalArgumentException if the no matching asset class can be found
   */
  public static AssetClass parse(String assetClass) {

    String upper = assetClass.toUpperCase();
    return Stream.of(values())
        .filter(ac -> ac.name().equals(upper) || ac.shortCode.equals(upper))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unable to parse an asset class from: " + assetClass));
  }
}
