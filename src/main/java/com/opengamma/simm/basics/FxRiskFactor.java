/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.simm.basics;

import java.util.Currency;

import com.opengamma.simm.utils.ArgChecker;

/**
 * An FX risk factor. Unlike a {@link StandardRiskFactor},
 * an FX risk factor is constrained such that it is represented by a single
 * currency.
 */
public class FxRiskFactor implements RiskFactor {

  /**
   * The currency this risk factor is for.
   */
  private final Currency currency;

  /**
   * Generate an {@code FxRiskFactorId} from the specified currency.
   *
   * @param currency  the currency of the risk factor
   * @return a new {@code FxRiskFactorId} for the currency
   * @throws IllegalArgumentException if currency is null
   */
  public static FxRiskFactor of(Currency currency) {
    return new FxRiskFactor(currency);
  }

  /**
   * Generate an {@code FxRiskFactorId} from the specified
   * String representing a currency.
   *
   * @param currency  the currency of the risk factor as a string
   * @return a new {@code FxRiskFactorId} for the currency
   * @throws IllegalArgumentException if currency is null or
   * is not a supported ISO 4217 code.
   */
  public static FxRiskFactor of(String currency) {
    ArgChecker.notEmpty(currency, "currency");
    return of(Currency.getInstance(currency));
  }

  // Private constructor
  private FxRiskFactor(Currency currency) {
    this.currency = ArgChecker.notNull(currency, "currency");
  }

  /**
   * Get the currency this risk factor is for.
   *
   * @return the risk factor's currency
   */
  public Currency getCurrency() {
    return currency;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    FxRiskFactor that = (FxRiskFactor) o;
    return currency.equals(that.currency);
  }

  @Override
  public int hashCode() {
    return currency.hashCode();
  }

  @Override
  public String toString() {
    return "FX risk factor (" + currency + ")";
  }
}
