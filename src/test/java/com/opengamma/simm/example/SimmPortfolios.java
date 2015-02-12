/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.simm.example;

import static com.opengamma.simm.example.SimmMarketData.*;
import static com.opengamma.simm.example.SimmMarketData.EUR_OIS_2Y;
import static com.opengamma.simm.example.SimmMarketData.EUR_OIS_5Y;
import static com.opengamma.simm.example.SimmMarketData.GBP_RF;
import static com.opengamma.simm.example.SimmMarketData.USD_IRSL3M_2Y;
import static com.opengamma.simm.util.CollectionUtils.createList;

import java.util.Currency;
import java.util.List;

import com.opengamma.simm.basics.PortfolioExposure;

public class SimmPortfolios {

  private static final Currency EUR = Currency.getInstance("EUR");
  private static final Currency GBP = Currency.getInstance("GBP");
  private static final Currency USD = Currency.getInstance("USD");

  public static final List<PortfolioExposure> DERIVATIVES = createList(
      PortfolioExposure.of(EUR_RF, 1000000, EUR),
      PortfolioExposure.of(USD_RF, -1400000, GBP),
      PortfolioExposure.of(GBP_RF, 200000, USD),
      PortfolioExposure.of(EUR_OIS_2Y, 100000, EUR),
      PortfolioExposure.of(EUR_OIS_5Y, -20000, EUR),
      PortfolioExposure.of(USD_IRSL3M_2Y, 20000, EUR),
      PortfolioExposure.of(IBM, 30300, EUR),
      PortfolioExposure.of(SP500, 100000, USD),
      PortfolioExposure.of(XAU, -123456, USD));

  public static final List<PortfolioExposure> VARIATION_MARGIN =
      createList(PortfolioExposure.of(GBP_RF, 150000, USD));

  public static final List<PortfolioExposure> INITIAL_MARGIN = createList(
      PortfolioExposure.of(USD_RF, -1300000, GBP),
      PortfolioExposure.of(USD_IRSL3M_2Y, 2000, EUR));
}
