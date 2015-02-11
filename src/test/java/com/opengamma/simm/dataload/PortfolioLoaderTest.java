/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.simm.dataload;

import static java.util.stream.Collectors.toSet;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.testng.annotations.Test;

import com.opengamma.simm.basics.FxRiskFactor;
import com.opengamma.simm.basics.StandardRiskFactor;
import com.opengamma.simm.basics.PortfolioExposure;
import com.opengamma.simm.basics.RiskFactor;

@Test
public class PortfolioLoaderTest {

  private static final Set<RiskFactor> NON_FX_RISK_FACTOR_NAMES =
      createNames("EUR-OIS-2Y", "EUR-OIS-5Y", "USD-IRSL3M-2Y", "IBM", "SP500", "XAU");

  private static final String BASE_DIR = "src/test/resources/";

  private static final String TEST_DIR = BASE_DIR + "parser-test/portfolios/";

  private static final String SAMPLE_DIR = BASE_DIR + "simm-sample/";

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "File.*could not be found.*")
  public void missingFileThrowsException() {
    loadTestFile("nowhere_to_be_found");
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "Expected header to contain.*")
  public void fileWithNoHeaderThrowsException() {
    loadTestFile("no-header");
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "Expected header to contain.*")
  public void fileWithWrongHeaderThrowsException() {
    loadTestFile("wrong-header");
  }

  @Test(
      expectedExceptions = NumberFormatException.class,
      expectedExceptionsMessageRegExp = "For input string.*")
  public void fileWithBadAmountThrowsException() {
    loadTestFile("bad-amount");
  }

  @Test(expectedExceptions = IllegalArgumentException.class /* No message in exception */)
  public void fileWithBadCurrencyThrowsException() {
    loadTestFile("bad-currency");
  }

  public void fileWithNoDataReturnsEmptyList() {
    List<PortfolioExposure> deltas = loadTestFile("no-data");
    assertTrue(deltas.isEmpty());
  }

  public void fileEntriesWithSameRiskFactorAndCurrencyAreAggregated() {
    List<PortfolioExposure> deltas = loadTestFile("aggregating");
    assertEquals(deltas.size(), 3);
  }

  public void fileWithCorrectDataReturnsPopulatedFxMatrix() {
    List<PortfolioExposure> deltas = loadSampleFile("portfolio-derivatives");
    assertEquals(deltas.size(), 9);
    // Check currencies read in with correct type of risk factor name
    assertEquals(deltas.stream().filter(rd -> rd.getRiskFactor() instanceof FxRiskFactor).count(), 3);
  }

  private List<PortfolioExposure> loadTestFile(String fileName) {
    return loadFile(TEST_DIR, fileName);
  }

  private List<PortfolioExposure> loadSampleFile(String fileName) {
    return loadFile(SAMPLE_DIR, fileName);
  }

  private List<PortfolioExposure> loadFile(String baseDir, String fileName) {
    return PortfolioLoader.of(new File(baseDir + fileName + ".csv"), NON_FX_RISK_FACTOR_NAMES).load();
  }

  private static Set<RiskFactor> createNames(String... names) {
    return Stream.of(names)
        .map(StandardRiskFactor::of)
        .collect(toSet());
  }

}