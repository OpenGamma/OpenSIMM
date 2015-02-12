/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.simm.load;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.simm.basics.StandardRiskFactor;
import com.opengamma.simm.basics.RiskFactor;

@Test
public class RiskFactorBaseLevelsLoaderTest {

  private static final String BASE_DIR = "src/test/resources/";
  private static final String SAMPLE_DIR = BASE_DIR + "simm-sample/";
  private static final String TEST_DIR = BASE_DIR + "parser-test/risk-factor-base-levels/";

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "File.*could not be found.*")
  public void missingFileThrowsException() {
    loadTestRiskFactorBaseLevels("nowhereto_be_found.csv");
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "Expected header to contain.*")
  public void fileWithNoHeaderThrowsException() {
    loadTestRiskFactorBaseLevels("no-header");
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "Expected header to contain.*")
  public void fileWithWrongHeaderThrowsException() {
    loadTestRiskFactorBaseLevels("wrong-header");
  }

  @Test(
      expectedExceptions = NumberFormatException.class,
      expectedExceptionsMessageRegExp = "For input string.*")
  public void fileWithBadDataThrowsException() {
    loadTestRiskFactorBaseLevels("bad-data");
  }

  public void fileWithNoDataReturnsEmptyFxMatrix() {
    Map<RiskFactor, Double> result = loadTestRiskFactorBaseLevels("no-data");
    assertTrue(result.isEmpty());
  }

  public void fileWithCorrectDataReturnsPopulatedFxMatrix() {
    Map<RiskFactor, Double> result = loadSampleRiskFactorBaseLevels("risk-factor-base-levels");
    assertEquals(result.size(), 6);
    check(result, "EUR-OIS-2Y", -0.0005);
    check(result, "EUR-OIS-5Y", 0.0025);
    check(result, "USD-IRSL3M-2Y", 0.01);
    check(result, "IBM", 0.012);
    check(result, "SP500", 1000);
    check(result, "XAU", 1200);
  }

  private Map<RiskFactor, Double> loadSampleRiskFactorBaseLevels(final String filename) {
    return loadRiskFactorLevels(SAMPLE_DIR, filename);
  }

  private Map<RiskFactor, Double> loadTestRiskFactorBaseLevels(final String filename) {
    return loadRiskFactorLevels(TEST_DIR, filename);
  }

  private Map<RiskFactor, Double> loadRiskFactorLevels(String dir, String filename) {
    return RiskFactorBaseLevelsLoader.of(new File(dir + filename + ".csv")).load();
  }

  private void check(Map<RiskFactor, Double> result, String name, double expected) {
    assertEquals(result.get(StandardRiskFactor.of(name)), expected);
  }

}