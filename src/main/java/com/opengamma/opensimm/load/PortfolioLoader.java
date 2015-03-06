/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.opensimm.load;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opengamma.opensimm.basics.FxRiskFactor;
import com.opengamma.opensimm.basics.PortfolioExposure;
import com.opengamma.opensimm.basics.RiskFactor;
import com.opengamma.opensimm.basics.StandardRiskFactor;
import com.opengamma.opensimm.util.ArgChecker;
import com.opengamma.opensimm.util.Pair;

/**
 * Loads portfolio risk exposure from a file, producing
 * a {@code List<PortfolioExposure>}.
 */
public class PortfolioLoader {

  private static final List<String> EXPECTED_HEADER =
      Arrays.asList("RiskFactorName", "Amount", "Currency");

  private final File file;
  private Set<RiskFactor> riskFactors;

  /**
   * Create a loader for the specified file.
   *
   * @param f  the file containing the portfolio risk exposures
   * @param riskFactors  the set of available risk factors
   * @return a new loader
   */
  public static PortfolioLoader of(File f, Set<RiskFactor> riskFactors) {
    return new PortfolioLoader(f, riskFactors);
  }

  /**
   * Load the portfolio risk exposure into a {@code List<PortfolioExposure>}.
   *
   * @return a {@code List<PortfolioExposure>} containing the
   *   portfolio risk exposure from the file
   */
  public List<PortfolioExposure> load() {

    return BasicCsvParser.parseFile(file, EXPECTED_HEADER, data ->
        data.map(this::convertToExposure)
          .collect(collectingAndThen(
              groupingBy(this::aggregationKey),
              this::aggregate)));
  }

  // Aggregate PortfolioExposures where the currency and
  // risk factors are the same
  private List<PortfolioExposure> aggregate(Map<Pair<RiskFactor, Currency>, List<PortfolioExposure>> groups) {
    return groups.entrySet()
        .stream()
        .map(e -> {
          double total = e.getValue()
              .stream()
              .mapToDouble(PortfolioExposure::getAmount)
              .sum();
          Pair<RiskFactor, Currency> key = e.getKey();
          return PortfolioExposure.of(key.getFirst(), total, key.getSecond());
        })
        .collect(toList());
  }

  private Pair<RiskFactor, Currency> aggregationKey(PortfolioExposure rd) {
    return Pair.of(rd.getRiskFactor(), rd.getCurrency());
  }

  private PortfolioExposure convertToExposure(List<String> row) {
    return PortfolioExposure.of(
        createRiskFactorName(row.get(0)),
        Double.parseDouble(row.get(1)),
        Currency.getInstance(row.get(2)));
  }

  private RiskFactor createRiskFactorName(String name) {
    // Either it's a user-supplied risk factor name, else
    // it's one for currency
    StandardRiskFactor nonFxName = StandardRiskFactor.of(name);
    return riskFactors.contains(nonFxName) ?
        nonFxName :
        FxRiskFactor.of(name);
  }

  private PortfolioLoader(File file, Set<RiskFactor> riskFactors) {
    checkFile(file);
    this.file = file;
    this.riskFactors = ArgChecker.notNull(riskFactors, "riskFactors");
  }

  private void checkFile(File file) {
    ArgChecker.notNull(file, "file");
    ArgChecker.isTrue(file.exists(), "File: {} could not be found", file);
  }

}
