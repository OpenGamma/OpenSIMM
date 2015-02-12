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

import com.opengamma.simm.basics.RiskFactor;
import com.opengamma.simm.basics.RiskFactorProperties;

@Test
public class RiskFactorDefinitionsLoaderTest {

  private static final String BASE_DIR = "src/test/resources/";
  private static final String SAMPLE_DIR = BASE_DIR + "simm-sample/";
  private static final String TEST_DIR = BASE_DIR + "parser-test/risk-factor-definitions/";

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "File.*could not be found.*")
  public void missingFileThrowsException() {
    loadTestRiskFactorDefinitions("nowhere_to_be_found");
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "Expected header to contain.*")
  public void fileWithNoHeaderThrowsException() {
    loadTestRiskFactorDefinitions("no-header");
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "Expected header to contain.*")
  public void fileWithWrongHeaderThrowsException() {
    loadTestRiskFactorDefinitions("wrong-header");
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "Unable to parse an asset class.*")
  public void fileWithBadAssetClassThrowsException() {
    // FX is in the enum but is not valid to be specified as a property
    loadTestRiskFactorDefinitions("bad-asset-class");
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "Unable to parse a risk type from.*")
  public void fileWithBadRiskTypeThrowsException() {
    loadTestRiskFactorDefinitions("bad-risk-type");
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "Unknown shift type.*")
  public void fileWithBadShiftTypeThrowsException() {
    loadTestRiskFactorDefinitions("bad-shift-type");
  }

  @Test(
      expectedExceptions = NumberFormatException.class,
      expectedExceptionsMessageRegExp = "For input string.*")
  public void fileWithBadShiftThrowsException() {
    loadTestRiskFactorDefinitions("bad-shift");
  }

  public void fileWithNoDataReturnsEmptyMap() {
    Map<RiskFactor, RiskFactorProperties> deltas = loadTestRiskFactorDefinitions("no-data");
    assertTrue(deltas.isEmpty());
  }

  public void additionalUnnecessaryShiftsAreIgnored() {
    Map<RiskFactor, RiskFactorProperties> deltas = loadTestRiskFactorDefinitions("additional-shifts");
    assertEquals(deltas.size(), 6);
  }

  public void fileWithCorrectDataReturnsPopulatedMap() {
    Map<RiskFactor, RiskFactorProperties> deltas = loadSampleRiskFactorDefinitions("risk-factor-definitions");
    assertEquals(deltas.size(), 6);
  }

  private Map<RiskFactor, RiskFactorProperties> loadTestRiskFactorDefinitions(String file) {
    return loadRiskFactorDefinitions(TEST_DIR, file);
  }

  private Map<RiskFactor, RiskFactorProperties> loadSampleRiskFactorDefinitions(String file) {
    return loadRiskFactorDefinitions(SAMPLE_DIR, file);
  }

  private Map<RiskFactor, RiskFactorProperties> loadRiskFactorDefinitions(String dir, String file) {
    return RiskFactorDefinitionsLoader.of(new File(dir + file + ".csv")).load();
  }

}