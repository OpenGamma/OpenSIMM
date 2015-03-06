/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.opensimm.load;

import com.opengamma.opensimm.basics.RiskFactor;
import com.opengamma.opensimm.basics.StandardRiskFactor;
import com.opengamma.opensimm.load.RiskFactorShocksLoader;

import org.testng.annotations.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

@Test
public class RiskFactorShocksLoaderTest {

  private static final String BASE_DIR = "src/test/resources/";
  private static final String SAMPLE_DIR = BASE_DIR + "simm-sample/";
  private static final String TEST_DIR = BASE_DIR + "parser-test/risk-factor-shocks/";

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "File.*could not be found.*")
  public void missingFileThrowsException() {
    loadTestRiskFactorShocks("nowhere_to_be_found");
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "Expected header to contain.*")
  public void fileWithNoHeaderThrowsException() {
    loadTestRiskFactorShocks("no-header");
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "Expected header to contain.*")
  public void fileWithWrongHeaderThrowsException() {
    loadTestRiskFactorShocks("wrong-header");
  }

  public void fileWithNoDataReturnsEmptyMap() {
    Map<RiskFactor, List<Double>> result =
        loadTestRiskFactorShocks("no-data");
    assertEquals(result.size(), 0);
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "No shocks found for risk factor.*")
  public void fileWithNoShocksThrowsException() {
    loadTestRiskFactorShocks("no-shocks");
  }

  public void fileWithCorrectDataReturnsPopulatedMap() {
    Map<RiskFactor, List<Double>> result =
        loadSampleRiskFactorShocks("risk-factor-shocks");
    assertEquals(result.size(), 6);
    assertEquals(result.get(StandardRiskFactor.of("EUR-OIS-2Y")),
        Arrays.asList(0.0001, -0.0005, -0.005, 0d, 0.0002, 0.0001, -0.0005, -0.005, 0.0001, -0.0005, -0.005, 0d, 0d,
            0.0002, 0.0001, -0.0005, -0.005, 0d, 0.0002, 0.0002, 0.0001, -0.0005, -0.005, 0d, 0.0002));
    assertEquals(result.get(StandardRiskFactor.of("XAU")),
        Arrays.asList(1.01, 0.995, 0.997, 1.001, 0.9955, 1.0002, 1.011, 0.994, 0.9975, 1.003, 0.99, 1.0004, 1.012, 0.9945));
  }

  private Map<RiskFactor, List<Double>> loadTestRiskFactorShocks(String file) {
    return loadRiskFactorShocks(TEST_DIR, file);
  }

  private Map<RiskFactor, List<Double>> loadSampleRiskFactorShocks(String file) {
    return loadRiskFactorShocks(SAMPLE_DIR, file);
  }

  private Map<RiskFactor, List<Double>> loadRiskFactorShocks(String dir, String file) {
    return RiskFactorShocksLoader.of(new File(dir + file + ".csv")).load();
  }

}