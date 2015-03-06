/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.opensimm.load;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.opensimm.load.FxShocksLoader;
import com.opengamma.opensimm.util.Pair;

@Test
public class FxShocksLoaderTest {

  private static final String BASE_DIR = "src/test/resources/";
  private static final String TEST_DIR = BASE_DIR + "parser-test/fx-rate-shocks/";
  private static final String SAMPLE_DIR = BASE_DIR + "simm-sample/";

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "File.*could not be found.*")
  public void missingFileThrowsException() {
    loadTestFxShocks("nowhereto_be_found");
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "Expected header to contain.*")
  public void fileWithNoHeaderThrowsException() {
    loadTestFxShocks("no-header");
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "Expected header to contain.*")
  public void fileWithWrongHeaderThrowsException() {
    loadTestFxShocks("wrong-header");
  }

  public void fileWithNoDataReturnsEmptyMap() {
    Map<Pair<Currency, Currency>, List<Double>> result =
        loadTestFxShocks("no-data");
    assertEquals(result.size(), 0);
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "No shocks found for currency pair.*")
  public void fileWithNoShocksThrowsException() {
    loadTestFxShocks("no-shocks");
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "All shocks must be the same length")
  public void fileWithMismatchingShocksLengthThrowsException() {
    loadTestFxShocks("mismatching-shock-lengths");
  }

  public void fileWithCorrectDataReturnsPopulatedMap() {
    Map<Pair<Currency, Currency>, List<Double>> result =
        loadSampleFxShocks("fx-rate-shocks");
    assertEquals(result.size(), 2);
    assertEquals(result.get(currencyPair("EUR", "USD")),
        Arrays.asList(1d, 1.001, 0.9975, 0.995, 1.0002, 1.01, 0.995, 0.997, 1.001, 1.0002, 1.01, 0.995, 0.997, 1.001,
            1.0002, 1.01, 0.995, 0.997, 1.001, 1.0002, 1.01, 0.995, 0.997, 1.001, 1.0002));
    assertEquals(result.get(currencyPair("GBP", "USD")),
        Arrays.asList(0.9985, 1.002, 1d, 0.995, 1.0003, 1.01, 0.994, 0.996, 1.001, 1.0002, 1.01, 1.005, 0.9999, 0.9999,
            1.0011, 1.005, 0.996, 0.998, 1.0025, 1.01, 0.995, 0.997, 1.001, 1.0002, 1.0001));
  }

  private Map<Pair<Currency, Currency>, List<Double>> loadSampleFxShocks(final String fileName) {
    return loadFxShocks(SAMPLE_DIR, fileName);
  }

  private Map<Pair<Currency, Currency>, List<Double>> loadTestFxShocks(final String fileName) {
    return loadFxShocks(TEST_DIR, fileName);
  }

  private Map<Pair<Currency, Currency>, List<Double>> loadFxShocks(String dir, String fileName) {
    return FxShocksLoader.of(new File(dir + fileName + ".csv")).load();
  }

  private Pair<Currency, Currency> currencyPair(String ccy1, String ccy2) {
    return Pair.of(Currency.getInstance(ccy1), Currency.getInstance(ccy2));
  }
}