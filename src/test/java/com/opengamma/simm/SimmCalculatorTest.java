/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.simm;

import static com.opengamma.simm.example.SimmMarketData.EUR;
import static org.testng.Assert.assertEquals;

import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.simm.basics.AssetClass;
import com.opengamma.simm.basics.FxMatrix;
import com.opengamma.simm.example.SimmMarketData;
import com.opengamma.simm.example.SimmPortfolios;

@Test
public class SimmCalculatorTest {
  
  private static final Currency BASE_CURRENCY = EUR;
  private static final double VAR_LEVEL = 0.90; // 90% VaR
  private static final double TOLERANCE = 1.0E-2;

  public void calculateVarWithNoExistingMarginInputs() {

    SimmCalculator calculator = buildSimmCalculator();

    Map<AssetClass, Double> var = calculator.varByAssetClass(SimmPortfolios.DERIVATIVES);

    Map<AssetClass, Double> expected = new HashMap<>();
    expected.put(AssetClass.COMMODITY, 564.3703);
    expected.put(AssetClass.CREDIT, 33.3300);
    expected.put(AssetClass.EQUITY, 740.7143);
    expected.put(AssetClass.INTEREST_RATE, 7660.9260);

    expected.entrySet()
        .stream()
        .forEach(e -> {
          AssetClass assetClass = e.getKey();
          assertEquals(var.get(assetClass), e.getValue(), TOLERANCE);
        });
  }

  public void calculateVarWithExistingMarginInputs() {

    SimmCalculator calculator = buildSimmCalculator();

    Map<AssetClass, Double> var = calculator.varByAssetClass(
        SimmPortfolios.DERIVATIVES, SimmPortfolios.INITIAL_MARGIN, SimmPortfolios.VARIATION_MARGIN);

    Map<AssetClass, Double> expected = new HashMap<>();
    expected.put(AssetClass.COMMODITY, 564.3703);
    expected.put(AssetClass.CREDIT, 33.3300);
    expected.put(AssetClass.EQUITY, 740.7143);
    expected.put(AssetClass.INTEREST_RATE, 447.5351);

    expected.entrySet()
        .stream()
        .forEach(e -> {
          AssetClass assetClass = e.getKey();
          assertEquals(var.get(assetClass), e.getValue(), TOLERANCE);
        });
  }

  private SimmCalculator buildSimmCalculator() {
    FxMatrix fxMatrix = SimmMarketData.FX_RATES
        .entrySet()
        .stream()
        .collect(FxMatrix.entryCollector());

    return SimmCalculator.builder()
        .varLevel(VAR_LEVEL)
        .baseCurrency(BASE_CURRENCY)
        .riskFactors(SimmMarketData.RISK_FACTOR_NON_FX)
        .riskFactorLevels(SimmMarketData.INITIAL_MARKET_LEVELS)
        .fxMatrix(fxMatrix)
        .riskFactorShocks(SimmMarketData.RF_SHOCKS)
        .fxShocks(SimmMarketData.FX_SHOCKS)
        .build();
  }

}
