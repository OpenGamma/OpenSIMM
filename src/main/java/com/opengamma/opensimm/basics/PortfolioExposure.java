/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.opensimm.basics;

import java.util.Currency;

import com.opengamma.opensimm.util.ArgChecker;

/**
 * Represents the exposure within a portfolio to
 * a particular risk factor.
 */
public class PortfolioExposure {

  /**
   * The risk factor.
   */
  private RiskFactor riskFactor;

  /**
   * The amount of exposure to the risk factor.
   */
  private double amount;

  /**
   * The currency of the exposure.
   */
  private Currency currency;

  /**
   * Creates a new {@code PortfolioExposure}.
   *
   * @param riskFactor  the risk factor
   * @param amount  the amount of the exposure
   * @param currency  the currency of the exposure
   * @return a new {@code PortfolioExposure}
   */
  public static PortfolioExposure of(RiskFactor riskFactor, double amount, Currency currency) {
    return new PortfolioExposure(riskFactor, amount, currency);
  }

  // Private constructor
  private PortfolioExposure(RiskFactor riskFactor, double amount, Currency currency) {
    this.riskFactor = ArgChecker.notNull(riskFactor, "riskFactor");
    this.amount = amount;
    this.currency = ArgChecker.notNull(currency, "currency");
  }

  /**
   * Returns the risk factor.
   *
   * @return the risk factor
   */
  public RiskFactor getRiskFactor() {
    return riskFactor;
  }

  /**
   * Returns the amount.
   *
   * @return the amount
   */
  public double getAmount() {
    return amount;
  }

  /**
   * Returns the currency.
   *
   * @return the currency
   */
  public Currency getCurrency() {
    return currency;
  }
}
