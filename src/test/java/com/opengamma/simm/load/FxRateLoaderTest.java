/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.simm.load;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.util.Currency;

import org.testng.annotations.Test;

import com.opengamma.simm.basics.FxMatrix;

@Test
public class FxRateLoaderTest {

  private static final double TOLERANCE = 1e-6;
  private static final String BASE_DIR = "src/test/resources/";
  private static final String TEST_DIR = BASE_DIR + "parser-test/fx-rates/";
  private static final String SAMPLE_DIR = BASE_DIR + "simm-sample/";

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "File.*could not be found.*")
  public void missingFileThrowsException() {
    loadTestRates("nowhereto_be_found.csv");
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "Expected header to contain.*")
  public void fileWithNoHeaderThrowsException() {
    loadTestRates("no-header");
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "Expected header to contain.*")
  public void fileWithWrongHeaderThrowsException() {
    loadTestRates("wrong-header");
  }

  @Test(expectedExceptions = NumberFormatException.class,
      expectedExceptionsMessageRegExp = "For input string.*")
  public void fileWithBadDataThrowsException() {
    loadTestRates("bad-data");
  }

  public void fileWithNoDataReturnsEmptyFxMatrix2() {
    FxMatrix fxMatrix = loadTestRates("no-data");
    assertEquals(fxMatrix.getCurrencies().size(), 0);
  }

  public void fileWithCorrectDataReturnsPopulatedFxMatrix2() {
    FxMatrix fxMatrix = loadSampleRates("fx-rates");
    assertEquals(fxMatrix.getCurrencies().size(), 3);

    assertEquals(fxRate(fxMatrix, "GBP", "USD"), 1.6);
    assertEquals(fxRate(fxMatrix, "USD", "GBP"), 1 / 1.6);
    assertEquals(fxRate(fxMatrix, "EUR", "USD"), 1.4);
    assertEquals(fxRate(fxMatrix, "USD", "EUR"), 1 / 1.4);
    assertEquals(fxRate(fxMatrix, "EUR", "GBP"), 1.4 / 1.6, TOLERANCE);
    assertEquals(fxRate(fxMatrix, "GBP", "EUR"), 1.6 / 1.4, TOLERANCE);
  }

  private FxMatrix loadSampleRates(final String fileName) {
    return loadRates(SAMPLE_DIR + fileName);
  }

  private FxMatrix loadTestRates(String fileName) {
    return loadRates(TEST_DIR + fileName);
  }

  private FxMatrix loadRates(String file) {
    return FxRateLoader.of(new File(file + ".csv")).load();
  }

  private double fxRate(FxMatrix fxMatrix, String ccy1, String ccy2) {
    return fxMatrix.getRate(Currency.getInstance(ccy1), Currency.getInstance(ccy2));
  }
}