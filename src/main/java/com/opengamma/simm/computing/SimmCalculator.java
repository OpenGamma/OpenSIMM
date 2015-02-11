/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.simm.computing;

import static com.opengamma.simm.basics.AssetClass.IR;
import static com.opengamma.simm.basics.RiskFactorProperties.RiskType.EXPOSURE;
import static com.opengamma.simm.computing.SimmUtils.percentile;
import static com.opengamma.simm.computing.SimmUtils.profits;
import static com.opengamma.simm.utils.CollectionUtils.createMap;
import static com.opengamma.simm.utils.CollectionUtils.pairsToMap;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.opengamma.simm.basics.AssetClass;
import com.opengamma.simm.basics.FxMatrix;
import com.opengamma.simm.basics.FxRiskFactor;
import com.opengamma.simm.basics.PortfolioExposure;
import com.opengamma.simm.basics.RiskFactor;
import com.opengamma.simm.basics.RiskFactorProperties;
import com.opengamma.simm.basics.RiskFactorProperties.RiskType;
import com.opengamma.simm.basics.ShockType;
import com.opengamma.simm.utils.ArgChecker;
import com.opengamma.simm.utils.Pair;

/**
 * An immutable calculator instance responsible for performing
 * the majority of the SIMM calculation.
 * <p>
 * On construction, the input data is used to construct the
 * shock structure which allows a VaR calculation to be performed
 * for a portfolio with exposure to some or all of the
 * risk factors.
 * <p>
 * A calculator cannot be constructed directly. Instead a
 * builder should be used which can be created using the
 * static {@link #builder()} method.
 * <p>
 * Once a calculator has been initialized, it can
 * be used to calculate VaR for multiple portfolios.
 */
public class SimmCalculator {

  private final double varLevel;
  private final FxMatrix fxMatrix;
  private final Currency baseCurrency;
  private final Map<RiskFactor, RiskFactorProperties> riskFactors;
  private final Map<RiskFactor, Double> riskFactorLevels;
  private final Map<AssetClass, Map<RiskFactor, List<Double>>> marketMovements;

  // Private constructor - use SimmCalculatorBuilder to create an instance
  private SimmCalculator(
      double varLevel,
      Currency baseCurrency,
      Map<RiskFactor, RiskFactorProperties> standardRiskFactors,
      Map<RiskFactor, Double> standardRiskFactorLevels,
      FxMatrix fxMatrix,
      Map<RiskFactor, List<Double>> riskFactorShocks,
      Map<Pair<Currency, Currency>, List<Double>> fxShocks) {

    this.varLevel = varLevel;
    this.fxMatrix = fxMatrix;
    this.baseCurrency = baseCurrency;
    this.riskFactors = generateRiskFactors(standardRiskFactors, fxMatrix);
    this.riskFactorLevels = generateRiskFactorLevels(baseCurrency, standardRiskFactorLevels, fxMatrix);

    Map<AssetClass, Map<RiskFactor, List<Double>>> shocks =
        generateShocks(baseCurrency, riskFactors, riskFactorShocks, fxShocks);

    // Compute market movements for each asset class
    this.marketMovements = calculateMarketMovements(riskFactors, riskFactorLevels, shocks);
  }

  /**
   * Create a mutable builder which can be used to construct a
   * {@code SimmCalculator} instance.
   *
   * @return a new builder
   */
  public static SimmCalculatorBuilder builder() {
    return new SimmCalculatorBuilder();
  }

  /**
   * Get the risk factors which this calculator has been initialized for.
   *
   * @return the set of risk factors this calculator uses
   */
  public Set<RiskFactor> getRiskFactors() {
    return riskFactors.keySet();
  }

  /**
   * Calculate the VaR by asset class for the specified portfolio exposures.
   *
   * @param derivatives  the risk factor exposures of the portfolio
   * @return the VaR by asset class
   */
  public Map<AssetClass, Double> varByAssetClass(List<PortfolioExposure> derivatives) {
    return varByAssetClass(convertExposures(derivatives));
  }

  /**
   * Calculate the VaR by asset class for the specified portfolio exposures.
   * This method allows any exposure of initial and variation margin to
   * be offset from the main portfolio exposures.
   *
   * @param derivatives  the risk factor exposures of the portfolio
   * @param initialMargin  the risk factor exposures of any initial margin
   * @param variationMargin  the risk factor exposures of any variation margin
   * @return the VaR by asset class
   */
  public Map<AssetClass, Double> varByAssetClass(
      List<PortfolioExposure> derivatives,
      List<PortfolioExposure> initialMargin,
      List<PortfolioExposure> variationMargin) {

    Map<RiskFactor, Double> converted = calculateExposureTotals(
        convertExposures(derivatives), convertExposures(initialMargin), convertExposures(variationMargin));
    return varByAssetClass(converted);
  }

  /**
   * Calculate the P&L vectors by asset class for the specified
   * portfolio exposures. This method allows any exposure of
   * initial and variation margin to be offset from the main portfolio
   * exposures.
   * <p>
   * The result for each asset class contains the ordered list of
   * P&L (highest to lowest), with a index (1-based) indicating
   * which shock was responsible for each value.
   *
   * @param derivatives  the risk factor exposures of the portfolio
   * @param initialMargin  the risk factor exposures of any initial margin
   * @param variationMargin  the risk factor exposures of any variation margin
   * @return the VaR by asset class
   */
  public Map<AssetClass, List<Pair<Integer, Double>>> pnlVectorsByAssetClass(
      List<PortfolioExposure> derivatives,
      List<PortfolioExposure> initialMargin,
      List<PortfolioExposure> variationMargin) {

    Map<RiskFactor, Double> converted = calculateExposureTotals(
        convertExposures(derivatives), convertExposures(initialMargin), convertExposures(variationMargin));

    return Collections.unmodifiableMap(
        marketMovements.entrySet()
            .stream()
            .collect(toMap(
                Map.Entry::getKey,
                // Type params added to keep eclipse happy
                (Map.Entry<AssetClass, Map<RiskFactor, List<Double>>> e) ->
                    pnlVectors(profits(e.getValue(), converted)))));
  }

  // Generate synthetic risk factors for the currencies in the
  // FX matrix and add them to the standard risk factors
  private Map<RiskFactor, RiskFactorProperties> generateRiskFactors(
      Map<RiskFactor, RiskFactorProperties> riskFactors,
      FxMatrix fxMatrix) {

    Map<RiskFactor, RiskFactorProperties> standardRiskFactors = new HashMap<>(riskFactors);
    standardRiskFactors.putAll(generateCurrencyRiskFactors(fxMatrix));
    return Collections.unmodifiableMap(standardRiskFactors);
  }

  // Generate synthetic risk factor levels for the currencies
  // using the rates in the matrix and add them to the standard
  // risk factor levels
  private Map<RiskFactor, Double> generateRiskFactorLevels(
      Currency baseCurrency,
      Map<RiskFactor, Double> riskFactorLevels,
      FxMatrix fxMatrix) {

    Map<RiskFactor, Double> levels = new HashMap<>(riskFactorLevels);
    levels.putAll(generateCurrencyRiskFactorLevels(baseCurrency, fxMatrix));
    return Collections.unmodifiableMap(levels);
  }

  // take the list of P&L (in shock order) and sort them from
  // highest to lowest keeping track of the original position
  private List<Pair<Integer, Double>> pnlVectors(List<Double> profits) {

    return IntStream.range(0, profits.size())
        .mapToObj(i -> Pair.of(i + 1, profits.get(i)))
        .sorted((p1, p2) -> (int) (p2.getSecond() - p1.getSecond()))
        .collect(toList());
  }

  // Take the portfolio exposures and convert so that it is
  // categorized by risk factor. Adjusts for both currency
  // and the risk type of the risk factor.
  public Map<RiskFactor, Double> convertExposures(List<PortfolioExposure> portfolioExposures) {

    return portfolioExposures.stream()
        .collect(groupingBy(PortfolioExposure::getRiskFactor))
        .entrySet()
        .stream()
        .collect(toMap(
            Map.Entry::getKey,
            e -> adjustExposures(e.getValue())));
  }

  // Aggregate and adjust a list of exposures
  private double adjustExposures(List<PortfolioExposure> exposures) {
    return exposures
        .stream()
        .mapToDouble(this::adjustExposure)
        .sum();
  }

  // Adjust the exposure for currency and risk type
  private double adjustExposure(PortfolioExposure pe) {
    RiskFactor riskFactor = pe.getRiskFactor();
    RiskType riskType = riskFactors.get(riskFactor).getRiskType();
    double fxRate = fxMatrix.getRate(pe.getCurrency(), baseCurrency);
    return pe.getAmount() * fxRate / (riskType == EXPOSURE ? riskFactorLevels.get(riskFactor) : 1);
  }

  private Map<AssetClass, Double> varByAssetClass(Map<RiskFactor, Double> riskFactorExposures) {
    return marketMovements.entrySet()
        .stream()
        .collect(toMap(
            Map.Entry::getKey,
            // Type params added to keep eclipse happy
            (Map.Entry<AssetClass, Map<RiskFactor, List<Double>>> e) ->
                percentile(profits(e.getValue(), riskFactorExposures), varLevel)));
  }

  private static Map<RiskFactor, Double> calculateExposureTotals(
      Map<RiskFactor, Double> adjustedDeltaDerivatives,
      Map<RiskFactor, Double> adjustedDeltaVariationMargin,
      Map<RiskFactor, Double> adjustedDeltaInitialMargin) {

    // Concatenate all keys together to ensure we have all keys no
    // matter which map they came from
    Stream<RiskFactor> combined = Stream.concat(
        adjustedDeltaDerivatives.keySet().stream(),
        Stream.concat(
            adjustedDeltaVariationMargin.keySet().stream(),
            adjustedDeltaInitialMargin.keySet().stream()));

    return combined
        .distinct()
        .collect(toMap(
            rf -> rf,
            rf -> adjustedDeltaDerivatives.getOrDefault(rf, 0d)
                - adjustedDeltaVariationMargin.getOrDefault(rf, 0d)
                - adjustedDeltaInitialMargin.getOrDefault(rf, 0d)
        ));
  }

  private Map<AssetClass, Map<RiskFactor, List<Double>>> calculateMarketMovements(
      Map<RiskFactor, RiskFactorProperties> riskFactors,
      Map<RiskFactor, Double> levels,
      Map<AssetClass, Map<RiskFactor, List<Double>>> shocks) {

    return shocks.entrySet()
        .stream()
        .collect(toMap(
            Map.Entry::getKey,
            e -> marketMovements(levels, e.getValue(), riskFactors)));
  }

  private Map<RiskFactor, List<Double>> marketMovements(
      Map<RiskFactor, Double> initialMarket,
      Map<RiskFactor, List<Double>> shocks,
      Map<RiskFactor, RiskFactorProperties> riskFactorsProperties){

    return Collections.<RiskFactor, List<Double>>unmodifiableMap(
        shocks.entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                // Type params added to keep eclipse happy
                (Map.Entry<RiskFactor, List<Double>> e) -> {
                  RiskFactor riskFactor = e.getKey();
                  RiskFactorProperties riskFactorProps = riskFactorsProperties.get(riskFactor);
                  double initialLevel = initialMarket.get(riskFactor);
                  ShockType shockType = riskFactorProps.getShockType();
                  return Collections.unmodifiableList(
                      e.getValue()
                          .stream()
                          .map(sh -> shockType.calculateShiftedValue(initialLevel, sh) - initialLevel)
                          .collect(Collectors.toList()));
                }
            )));
  }

  // Supplement the supplied risk factors with ones
  // generated from the currencies in play
  private Map<RiskFactor, RiskFactorProperties> generateCurrencyRiskFactors(FxMatrix fxMatrix) {
    // Fx risk factors are included in the IR asset class
    return generateCurrencyRiskFactors(fxMatrix, ccy -> RiskFactorProperties.relativeShock(IR, EXPOSURE, 0));
  }

  private  Map<RiskFactor, Double> generateCurrencyRiskFactorLevels(Currency baseCcy, FxMatrix fxMatrix) {
    return generateCurrencyRiskFactors(fxMatrix, ccy -> fxMatrix.getRate(baseCcy, ccy));
  }

  private <T> Map<RiskFactor, T> generateCurrencyRiskFactors(
      FxMatrix fxMatrix, Function<Currency, T> valueExtractor) {

    return fxMatrix.getCurrencies()
        .stream()
        .collect(toMap(
            FxRiskFactor::of, valueExtractor));
  }

  private Map<AssetClass, Map<RiskFactor, List<Double>>> generateShocks(
      Currency baseCcy,
      Map<RiskFactor, RiskFactorProperties> riskFactorProperties,
      Map<RiskFactor, List<Double>> riskFactorShocks,
      Map<Pair<Currency, Currency>, List<Double>> fxShocks) {

    // Create a stream containing the basic risk factor shocks and the FX shocks
    Stream<Pair<RiskFactor, List<Double>>> combined = Stream.concat(
        riskFactorShocks.entrySet().stream().map(e -> Pair.of(e.getKey(), e.getValue())),
        rebaseFxShocks(baseCcy, riskFactorProperties.keySet(), fxShocks));

    return combined.collect(
        groupingBy(
            p -> riskFactorProperties.get(p.getFirst()).getAssetClass(),
            pairsToMap()));
  }

  // Take the shocks defined in terms of currency pairs and convert
  // them so they are shocks against a single currency with respect
  // to the base currency
  private Stream<Pair<RiskFactor, List<Double>>> rebaseFxShocks(
      Currency baseCcy,
      Set<RiskFactor> riskFactors,
      Map<Pair<Currency, Currency>, List<Double>> fxShocks) {

    List<FxMatrix> matrices = fxShocks.entrySet()
        .stream()
        .map(e -> e.getValue().stream().map(d -> createMap(e.getKey(), d)).collect(toList()))
        .reduce((l1, l2) ->
            IntStream.range(0, l1.size())
                .mapToObj(i -> mergeMaps(l1.get(i), l2.get(i)))
                .collect(toList()))
        .map(l ->
            l.stream()
                .map(m ->
                    m.entrySet()
                        .stream()
                        .collect(FxMatrix.entryCollector()))
                .collect(toList()))
        .orElse(new ArrayList<>());

    return riskFactors
        .stream()
        // Only want the FX risk factors
        .filter(rf -> rf instanceof FxRiskFactor)
        .map(rf -> (FxRiskFactor) rf)
        .map(rf -> Pair.of(
            rf,
            matrices.stream()
                .map(mx -> mx.getRate(baseCcy, rf.getCurrency()))
                .collect(toList())));
  }

  // Combine a pair of maps with distinct into one
  private <K, V> Map<K, V> mergeMaps(Map<K, V> m1, Map<K, V> m2) {
    Map<K, V> m = new HashMap<>();
    m.putAll(m1);
    m.putAll(m2);
    return m;
  }

  /**
   * Mutable builder for creating a SimmCalculator instance.
   */
  public static class SimmCalculatorBuilder {

    /**
     * By default VaR is calculated using a 99% confidence level.
     */
    private static final double DEFAULT_VAR_LEVEL = 0.99;

    private double varLevel = DEFAULT_VAR_LEVEL;
    private Currency baseCurrency;
    private Map<RiskFactor, RiskFactorProperties> standardRiskFactors = new HashMap<>();
    private Map<RiskFactor, Double> standardRiskFactorLevels = new HashMap<>();
    private FxMatrix fxMatrix = FxMatrix.EMPTY_FX_MATRIX;
    private Map<RiskFactor, List<Double>> riskFactorShocks = new HashMap<>();
    private Map<Pair<Currency, Currency>, List<Double>> fxShocks = new HashMap<>();

    // Private constructor, use SimmCalculator.builder();
    private SimmCalculatorBuilder() {
    }

    /**
     * Build a {@code SimmCalculator} using the data defined in the builder.
     * Validation is performed that the data is self-consistent.
     *
     * @return a new {@code SimmCalculator}
     */
    public SimmCalculator build() {
      ArgChecker.notNull(baseCurrency, "baseCurrency"); // Only field which could have been null

      checkCurrencies(baseCurrency, fxMatrix, fxShocks);
      checkRiskFactorLevels(standardRiskFactors, standardRiskFactorLevels);
      checkShockLengths(standardRiskFactors, riskFactorShocks, fxShocks);

      return new SimmCalculator(
          varLevel, baseCurrency, standardRiskFactors, standardRiskFactorLevels, fxMatrix, riskFactorShocks, fxShocks);
    }

    /**
     * Set the var confidence level to be used.
     * A value of 0.9 represents a 90% confidence level.
     *
     * @param varLevel  the var confidence level
     * @return the builder
     */
    public SimmCalculatorBuilder varLevel(double varLevel) {
      this.varLevel = ArgChecker.inRangeExcludingHigh(0, 1, varLevel, "varLevel");
      return this;
    }

    /**
     * Set the base currency for the calculator.
     * Values returned by the calculator will be expressed in
     * this currency.
     *
     * @param baseCurrency  the base currency for all calculations
     * @return the builder
     */
    public SimmCalculatorBuilder baseCurrency(Currency baseCurrency) {
      this.baseCurrency = ArgChecker.notNull(baseCurrency, "baseCurrency");
      return this;
    }

    /**
     * Set the risk factors and their properties for the calculator.
     *
     * @param riskFactors  the risk factors to be used
     * @return the builder
     */
    public SimmCalculatorBuilder riskFactors(Map<RiskFactor, RiskFactorProperties> riskFactors) {
      this.standardRiskFactors = ArgChecker.notNull(riskFactors, "standardRiskFactors");
      return this;
    }

    /**
     * Set the base risk factor levels for the calculator. These
     * are the values that will be adjusted as shocks are applied.
     *
     * @param riskFactorLevels  the base risk factor levels
     * @return the builder
     */
    public SimmCalculatorBuilder riskFactorLevels(Map<RiskFactor, Double> riskFactorLevels) {
      this.standardRiskFactorLevels = ArgChecker.notNull(riskFactorLevels, "standardRiskFactorLevels");
      return this;
    }

    /**
     * Set the FX matrix containing the current FX rates for the calculator.
     *
     * @param fxMatrix  the FX matrix to be used
     * @return the builder
     */
    public SimmCalculatorBuilder fxMatrix(FxMatrix fxMatrix) {
      this.fxMatrix = ArgChecker.notNull(fxMatrix, "fxMatrix");
      return this;
    }

    /**
     * Set the risk factor shocks for the calculator.
     *
     * @param riskFactorShocks  the risk factor shocks
     * @return the builder
     */
    public SimmCalculatorBuilder riskFactorShocks(Map<RiskFactor, List<Double>> riskFactorShocks) {
      this.riskFactorShocks = ArgChecker.notNull(riskFactorShocks, "riskFactorShocks");
      return this;
    }

    /**
     * Set the FX shocks for the calculator.
     *
     * @param fxShocks  the FX shocks
     * @return the builder
     */
    public SimmCalculatorBuilder fxShocks(Map<Pair<Currency, Currency>, List<Double>> fxShocks) {
      this.fxShocks = ArgChecker.notNull(fxShocks, "fxShocks");
      return this;
    }

    private void checkCurrencies(
        Currency baseCurrency,
        FxMatrix fxMatrix,
        Map<Pair<Currency, Currency>, List<Double>> fxShocks) {

      if (!fxMatrix.getCurrencies().contains(baseCurrency)) {
        throw new IllegalStateException("FX Matrix with currencies: " + fxMatrix.getCurrencies() +
            " does not contain the base currency: " + baseCurrency);
      }

      Set<Currency> currencies = fxMatrix.getCurrencies();

      Set<Currency> missingCurrencies = fxShocks.keySet()
          .stream()
          .flatMap(e -> Stream.of(e.getFirst(), e.getSecond()))
          .filter(ccy -> !currencies.contains(ccy))
          .collect(toSet());

      if (!missingCurrencies.isEmpty()) {
        throw new IllegalStateException(
            "fxShocks contains currencies which are not in the fx matrix: " + missingCurrencies);
      }
    }

    private void checkRiskFactorLevels(
        Map<RiskFactor, RiskFactorProperties> riskFactors,
        Map<RiskFactor, Double> riskFactorLevels) {

      Set<RiskFactor> unknownFactors = riskFactorLevels.keySet()
          .stream()
          .filter(rf -> !riskFactors.containsKey(rf))
          .collect(toSet());

      if (!unknownFactors.isEmpty()) {
        throw new IllegalStateException("standardRiskFactorLevels contains unknown risk factors: " + unknownFactors);
      }
    }

    private void checkShockLengths(
        Map<RiskFactor, RiskFactorProperties> riskFactors,
        Map<RiskFactor, List<Double>> riskFactorShocks,
        Map<Pair<Currency, Currency>, List<Double>> fxShocks) {

      Set<Integer> fxShockSizes = fxShocks.values()
          .stream()
          .map(List::size)
          .distinct()
          .collect(toSet());

      if (fxShockSizes.size() != 1) {
        throw new IllegalStateException(
            "fxShocks must all be the same length");
      }

      Map<AssetClass, Set<Integer>> shockLengths = riskFactorShocks.entrySet()
          .stream()
          .map(e -> Pair.of(e.getKey(), e.getValue().size()))
          .collect(
              groupingBy(
                  p -> riskFactors.get(p.getFirst()).getAssetClass(),
                  mapping(Pair::getSecond, toSet())));

      Set<AssetClass> badShockLengths = shockLengths
          .entrySet()
          .stream()
          .filter(e -> e.getValue().size() > 1)
          .map(Map.Entry::getKey)
          .collect(toSet());

      if (!badShockLengths.isEmpty()) {
        throw new IllegalStateException(
            "Shocks for risk factors with the same asset class must all be the same length - " + badShockLengths +
                " have differing lengths");
      }

      int fxShocksSize = fxShockSizes.iterator().next();
      int irShocksSize = shockLengths.get(IR).iterator().next();
      if (fxShocksSize != irShocksSize) {
        throw new IllegalStateException(
            "Number of fxShocks [" + fxShocksSize +
                "] must be the same as number of IR shocks [" + irShocksSize + "]");
      }
    }

  }

}
