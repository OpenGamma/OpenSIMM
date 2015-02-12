/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.simm.loading;


import static com.opengamma.simm.util.CollectionUtils.createMap;
import static com.opengamma.simm.util.CollectionUtils.entriesToMap;
import static com.opengamma.simm.util.CollectionUtils.pairsToMap;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import com.opengamma.simm.basics.FxMatrix;
import com.opengamma.simm.basics.StandardRiskFactor;
import com.opengamma.simm.basics.PortfolioExposure;
import com.opengamma.simm.basics.RiskFactor;
import com.opengamma.simm.basics.RiskFactorProperties;
import com.opengamma.simm.basics.AssetClass;
import com.opengamma.simm.SimmCalculator;
import com.opengamma.simm.load.FxRateLoader;
import com.opengamma.simm.load.FxShocksLoader;
import com.opengamma.simm.load.PortfolioLoader;
import com.opengamma.simm.load.RiskFactorBaseLevelsLoader;
import com.opengamma.simm.load.RiskFactorDefinitionsLoader;
import com.opengamma.simm.load.RiskFactorShocksLoader;
import com.opengamma.simm.util.Pair;

@Test
public class SimmFileLoadingTest {

  private static final String BASE_DIR = "src/test/resources/simm-sample/";

  private static final Currency USD = Currency.getInstance("USD");

  private static final Currency EUR = Currency.getInstance("EUR");
  private static final Currency GBP = Currency.getInstance("GBP");

  public void sampleCalculation() {

    SimmCalculator calculator = SimmCalculator.builder()
        .varLevel(0.9)
        .baseCurrency(EUR)
        .riskFactors(loadRiskFactorDefinitions())
        .riskFactorLevels(loadRiskFactorLevels())
        .fxMatrix(loadFxMatrix())
        .riskFactorShocks(loadRiskFactorShocks())
        .fxShocks(loadFxShocks())
        .build();

    Set<RiskFactor> riskFactors = calculator.getRiskFactors();
    List<PortfolioExposure> derivativesPortfolio = loadPortfolio("portfolio-derivatives", riskFactors);
    List<PortfolioExposure> initialMargin = loadPortfolio("portfolio-initial-margin", riskFactors);
    List<PortfolioExposure> variationMargin = loadPortfolio("portfolio-variation-margin", riskFactors);

    Map<AssetClass, Double> var =
        calculator.varByAssetClass(derivativesPortfolio, initialMargin, variationMargin);
    assertEquals(var.size(), 4);

    Map<AssetClass, Double> expected = createMap(
        AssetClass.INTEREST_RATE, 447.5351,
        AssetClass.CREDIT, 33.3300,
        AssetClass.EQUITY, 740.7143,
        AssetClass.COMMODITY, 564.3703);

    expected.entrySet()
        .stream()
        .forEach(e -> {
          AssetClass assetClass = e.getKey();
          assertEquals(var.get(assetClass), e.getValue(), 1e-4);
        });
  }

  @Test(
      expectedExceptions = IllegalStateException.class,
      expectedExceptionsMessageRegExp = "FX Matrix.*does not contain the base currency.*")
  public void fxRatesMustIncludeBaseCurrency() {

    Map<RiskFactor, RiskFactorProperties> riskFactors = loadRiskFactorDefinitions();

    FxMatrix fxMatrix = FxMatrix.builder()
        .addRate(GBP, USD, 1.6)
        .build();

    SimmCalculator.builder()
        .varLevel(0.9)
        .baseCurrency(Currency.getInstance("EUR"))
        .riskFactors(riskFactors)
        .riskFactorLevels(loadRiskFactorLevels())
        .fxMatrix(fxMatrix)
        .riskFactorShocks(loadRiskFactorShocks())
        .fxShocks(loadFxShocks())
        .build();
  }

  @Test(
      expectedExceptions = IllegalStateException.class,
      expectedExceptionsMessageRegExp = ".*contains unknown risk factors.*IBM.*")
  public void riskFactorLevelsMustUseKnownRiskFactors() {

    // Remove one of the risk factors from the standard set
    Map<RiskFactor, RiskFactorProperties> riskFactors =
        loadRiskFactorDefinitions()
            .entrySet()
            .stream()
            .filter(e -> !e.getKey().equals(StandardRiskFactor.of("IBM")))
            .collect(entriesToMap());

    SimmCalculator.builder()
        .varLevel(0.9)
        .baseCurrency(Currency.getInstance("EUR"))
        .riskFactors(riskFactors)
        .riskFactorLevels(loadRiskFactorLevels())
        .fxMatrix(loadFxMatrix())
        .riskFactorShocks(loadRiskFactorShocks())
        .fxShocks(loadFxShocks())
        .build();
  }

  @Test(
      expectedExceptions = IllegalStateException.class,
      expectedExceptionsMessageRegExp = "fxShocks contains currencies which are not in the fx matrix.*")
  public void fxRatesMustIncludeCurrenciesUsedInFxShocks() {

    Map<RiskFactor, RiskFactorProperties> riskFactors = loadRiskFactorDefinitions();

    FxMatrix fxMatrix = FxMatrix.builder()
        .addRate(EUR, USD, 1.6)
        .build();

    // FX shocks contain GBP/EUR but we have no rate for GBP

    SimmCalculator.builder()
        .varLevel(0.9)
        .baseCurrency(Currency.getInstance("EUR"))
        .riskFactors(riskFactors)
        .riskFactorLevels(loadRiskFactorLevels())
        .fxMatrix(fxMatrix)
        .riskFactorShocks(loadRiskFactorShocks())
        .fxShocks(loadFxShocks())
        .build();
  }

  @Test(
      expectedExceptions = IllegalStateException.class,
      expectedExceptionsMessageRegExp = "fxShocks must all be the same length.*")
  public void fxRateShocksMustAllBeSameLength() {

    Map<Pair<Currency, Currency>, List<Double>> fxShocks = loadFxShocks();
    Map<Pair<Currency, Currency>, List<Double>> updated = new HashMap<>(fxShocks);
    updated.put(Pair.of(EUR, USD), Arrays.asList(1d, 1.001, 0.9975, 0.995, 1.0002, 1.01, 0.995, 0.997));

    SimmCalculator.builder()
        .varLevel(0.9)
        .baseCurrency(Currency.getInstance("EUR"))
        .riskFactors(loadRiskFactorDefinitions())
        .riskFactorLevels(loadRiskFactorLevels())
        .fxMatrix(loadFxMatrix())
        .riskFactorShocks(loadRiskFactorShocks())
        .fxShocks(updated)
        .build();
  }

  @Test(
      expectedExceptions = IllegalStateException.class,
      expectedExceptionsMessageRegExp =
          "Shocks for risk factors with the same asset class must all be the same length.*")
  public void riskFactorShocksMustBeSameLengthPerAssetClass() {

    Map<RiskFactor, List<Double>> riskFactorShocks = new HashMap<>(loadRiskFactorShocks());
    // Replace EUR-OIS-2Y with a shortened shock sequence
    riskFactorShocks.put(StandardRiskFactor.of("EUR-OIS-2Y"),
        Arrays.asList(0.0001, -0.0005, -0.005, 0d, 0.0002, 0.0001, -0.0005, -0.005, 0.0001));

    SimmCalculator.builder()
        .varLevel(0.9)
        .baseCurrency(Currency.getInstance("EUR"))
        .riskFactors(loadRiskFactorDefinitions())
        .riskFactorLevels(loadRiskFactorLevels())
        .fxMatrix(loadFxMatrix())
        .riskFactorShocks(riskFactorShocks)
        .fxShocks(loadFxShocks())
        .build();
  }

  @Test(
      expectedExceptions = IllegalStateException.class,
      expectedExceptionsMessageRegExp = "Number of fxShocks.*must be the same as number of IR shocks.*")
  public void fxRateShocksMustAllBeSameLengthAsIrShocks() {

    Map<Pair<Currency, Currency>, List<Double>> fxShocks = loadFxShocks();

    // Make each fx shock 1 item shorter
    Map<Pair<Currency, Currency>, List<Double>> updated = fxShocks.entrySet()
        .stream()
        .map(e -> Pair.of(e.getKey(), e.getValue().stream().skip(1).collect(toList())))
        .collect(pairsToMap());

    SimmCalculator.builder()
        .varLevel(0.9)
        .baseCurrency(Currency.getInstance("EUR"))
        .riskFactors(loadRiskFactorDefinitions())
        .riskFactorLevels(loadRiskFactorLevels())
        .fxMatrix(loadFxMatrix())
        .riskFactorShocks(loadRiskFactorShocks())
        .fxShocks(updated)
        .build();
  }

  private List<PortfolioExposure> loadPortfolio(String filename, Set<RiskFactor> riskFactors) {
    return PortfolioLoader.of(getFile(filename), riskFactors).load();
  }

  private Map<RiskFactor, RiskFactorProperties> loadRiskFactorDefinitions() {
    return RiskFactorDefinitionsLoader.of(getFile("risk-factor-definitions")).load();
  }

  private Map<RiskFactor, Double> loadRiskFactorLevels() {
    return RiskFactorBaseLevelsLoader.of(getFile("risk-factor-base-levels")).load();
  }

  private FxMatrix loadFxMatrix() {
    return FxRateLoader.of(getFile("fx-rates")).load();
  }

  private Map<RiskFactor, List<Double>> loadRiskFactorShocks() {
    return RiskFactorShocksLoader.of(getFile("risk-factor-shocks")).load();
  }

  private Map<Pair<Currency, Currency>, List<Double>> loadFxShocks() {
    return FxShocksLoader.of(getFile("fx-rate-shocks")).load();
  }

  private File getFile(String name) {
    return new File(BASE_DIR + name + ".csv");
  }

}
