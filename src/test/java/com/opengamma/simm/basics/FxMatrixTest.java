/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.simm.basics;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Currency;
import java.util.LinkedHashMap;

import org.testng.annotations.Test;

import com.opengamma.simm.util.Pair;

@Test
public class FxMatrixTest {

  private static final double TOLERANCE = 1e-6;;

  private static final Currency USD = Currency.getInstance("USD");

  private static final Currency EUR = Currency.getInstance("EUR");
  private static final Currency GBP = Currency.getInstance("GBP");
  private static final Currency JPY = Currency.getInstance("JPY");
  private static final Currency CAD = Currency.getInstance("CAD");
  private static final Currency AUD = Currency.getInstance("AUD");
  private static final Currency NZD = Currency.getInstance("NZD");
  private static final Currency CHF = Currency.getInstance("CHF");
  private static final Currency SEK = Currency.getInstance("SEK");

  public void emptyMatrixCanHandleTrivialRate() {
    FxMatrix matrix = FxMatrix.builder().build();
    assertTrue(matrix.getCurrencies().isEmpty());
    assertEquals(matrix.getRate(USD, USD), 1.0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void emptyMatrixCannotDoConversion() {
    FxMatrix matrix = FxMatrix.builder().build();
    assertTrue(matrix.getCurrencies().isEmpty());
    matrix.getRate(USD, EUR);
  }

  public void singleRateMatrix() {
    FxMatrix matrix = FxMatrix.builder()
        .addRate(GBP, USD, 1.6)
        .build();
    assertEquals(matrix.getCurrencies().size(), 2);
    assertEquals(matrix.getRate(GBP, USD), 1.6);
    assertEquals(matrix.getRate(USD, GBP), 0.625);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void singleRateMatrixCannotDoConversionForUnknownCurrency() {
    FxMatrix matrix = FxMatrix.builder()
        .addRate(GBP, USD, 1.6)
        .build();
    assertEquals(matrix.getCurrencies().size(), 2);
    matrix.getRate(USD, EUR);
  }

  public void matrixCalculatesCrossRates() {

    FxMatrix matrix = FxMatrix.builder()
        .addRate(GBP, USD, 1.6)
        .addRate(EUR, USD, 1.4)
        .addRate(EUR, CHF, 1.2)
        .build();

    assertEquals(matrix.getCurrencies().size(), 4);

    assertEquals(matrix.getRate(GBP, USD), 1.6);
    assertEquals(matrix.getRate(USD, GBP), 1 / 1.6);
    assertEquals(matrix.getRate(EUR, USD), 1.4);
    assertEquals(matrix.getRate(USD, EUR), 1 / 1.4);
    assertEquals(matrix.getRate(EUR, GBP), 1.4 / 1.6, TOLERANCE);
    assertEquals(matrix.getRate(GBP, EUR), 1.6 / 1.4, TOLERANCE);
    assertEquals(matrix.getRate(EUR, CHF), 1.2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void cannotAddEntryWithNoCommonCurrency() {
    FxMatrix.builder()
        .addRate(GBP, USD, 1.6)
        .addRate(CHF, AUD, 1.6);
  }

  public void rateCanBeUpdatedInBuilder() {
    FxMatrix matrix = FxMatrix.builder()
        .addRate(GBP, USD, 1.5)
        .addRate(GBP, USD, 1.6)
        .build();
    assertEquals(matrix.getCurrencies().size(), 2);
    assertEquals(matrix.getRate(GBP, USD), 1.6);
    assertEquals(matrix.getRate(USD, GBP), 0.625);
  }

  public void ratedCanBeUpdatedAndAddedViaBuilder() {
    FxMatrix matrix1 = FxMatrix.builder()
        .addRate(GBP, USD, 1.5)
        .build();

    assertEquals(matrix1.getCurrencies().size(), 2);
    assertEquals(matrix1.getRate(GBP, USD), 1.5);

    FxMatrix matrix2 = matrix1.toBuilder()
        .addRate(GBP, USD, 1.6)
        .addRate(EUR, USD, 1.4)
        .build();

    assertEquals(matrix2.getCurrencies().size(), 3);
    assertEquals(matrix2.getRate(GBP, USD), 1.6);
    assertEquals(matrix2.getRate(EUR, USD), 1.4);
  }

  public void ratedCanBeUpdatedWithDirectionSwitched() {
    FxMatrix matrix1 = FxMatrix.builder()
        .addRate(GBP, USD, 1.6)
        .build();

    assertEquals(matrix1.getCurrencies().size(), 2);
    assertEquals(matrix1.getRate(GBP, USD), 1.6);

    FxMatrix matrix2 = matrix1.toBuilder()
        .addRate(USD, GBP, 0.625)
        .build();

    assertEquals(matrix2.getCurrencies().size(), 2);
    assertEquals(matrix2.getRate(GBP, USD), 1.6);
  }


  public void addSimpleMultipleRates() {

    // Use linked to force the order of evaluation
    // want to see that builder recovers when
    // encountering a currency pair for 2 unknown
    // currencies but which will appear later
    LinkedHashMap<Pair<Currency, Currency>, Double> rates = new LinkedHashMap<>();
    rates.put(currencyPair(GBP, USD), 1.6);
    rates.put(currencyPair(EUR, USD), 1.4);

    FxMatrix matrix = FxMatrix.builder()
        .addRates(rates)
        .build();

    assertEquals(matrix.getRate(GBP, USD), 1.6);
    assertEquals(matrix.getRate(USD, GBP), 1 / 1.6);
    assertEquals(matrix.getRate(EUR, USD), 1.4);
    assertEquals(matrix.getRate(USD, EUR), 1 / 1.4);
    assertEquals(matrix.getRate(EUR, GBP), 1.4 / 1.6, TOLERANCE);
    assertEquals(matrix.getRate(GBP, EUR), 1.6 / 1.4, TOLERANCE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void addMultipleRatesContainingEntryWithNoCommonCurrency() {

    LinkedHashMap<Pair<Currency, Currency>, Double> rates = new LinkedHashMap<>();
    rates.put(currencyPair(GBP, USD), 1.6);
    rates.put(currencyPair(EUR, USD), 1.4);
    rates.put(currencyPair(JPY, CAD), 0.01); // Neither currency linked to one of the others

    FxMatrix.builder().addRates(rates);
  }

  public void addMultipleRates() {

    // Use linked map to force the order of evaluation
    // want to see that builder recovers when
    // encountering a currency pair for 2 unknown
    // currencies but which will appear later
    LinkedHashMap<Pair<Currency, Currency>, Double> rates = new LinkedHashMap<>();
    rates.put(currencyPair(GBP, USD), 1.6);
    rates.put(currencyPair(EUR, USD), 1.4);
    rates.put(currencyPair(CHF, AUD), 1.2);  // Neither currency seen before
    rates.put(currencyPair(SEK, AUD), 0.16); // AUD seen before but not added yet
    rates.put(currencyPair(JPY, CAD), 0.01); // Neither currency seen before
    rates.put(currencyPair(EUR, CHF), 1.2);
    rates.put(currencyPair(JPY, USD), 0.0084);

    FxMatrix matrix = FxMatrix.builder()
        .addRates(rates)
        .build();

    assertEquals(matrix.getRate(GBP, USD), 1.6);
    assertEquals(matrix.getRate(USD, GBP), 1 / 1.6);
    assertEquals(matrix.getRate(EUR, USD), 1.4);
    assertEquals(matrix.getRate(USD, EUR), 1 / 1.4);
    assertEquals(matrix.getRate(EUR, GBP), 1.4 / 1.6, TOLERANCE);
    assertEquals(matrix.getRate(GBP, EUR), 1.6 / 1.4, TOLERANCE);
    assertEquals(matrix.getRate(EUR, CHF), 1.2);
  }

  // By adding more than 8 currencies we force a resizing
  // operation - ensure it causes no issues
  public void addMultipleRatesSingle() {

    FxMatrix matrix = FxMatrix.builder()
        .addRate(GBP, USD, 1.6)
        .addRate(EUR, USD, 1.4)
        .addRate(EUR, CHF, 1.2)
        .addRate(EUR, CHF, 1.2)
        .addRate(CHF, AUD, 1.2)
        .addRate(SEK, AUD, 0.16)
        .addRate(JPY, USD, 0.0084)
        .addRate(JPY, CAD, 0.01)
        .addRate(USD, NZD, 1.3)
        .build();

    assertEquals(matrix.getRate(GBP, USD), 1.6);
    assertEquals(matrix.getRate(USD, GBP), 1 / 1.6);
    assertEquals(matrix.getRate(EUR, USD), 1.4);
    assertEquals(matrix.getRate(USD, EUR), 1 / 1.4);
    assertEquals(matrix.getRate(EUR, GBP), 1.4 / 1.6, TOLERANCE);
    assertEquals(matrix.getRate(GBP, EUR), 1.6 / 1.4, TOLERANCE);
    assertEquals(matrix.getRate(EUR, CHF), 1.2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void cannotMergeDisjointMatrices() {

    FxMatrix matrix1 = FxMatrix.builder()
        .addRate(GBP, USD, 1.6)
        .addRate(EUR, USD, 1.4)
        .build();

    FxMatrix matrix2 = FxMatrix.builder()
        .addRate(CHF, AUD, 1.2)
        .addRate(SEK, AUD, 0.16)
        .build();

    matrix1.merge(matrix2);
  }

  public void mergeIgnoresDuplicateCurrencies() {

    FxMatrix matrix1 = FxMatrix.builder()
        .addRate(GBP, USD, 1.6)
        .addRate(EUR, USD, 1.4)
        .addRate(EUR, CHF, 1.2)
        .build();

    FxMatrix matrix2 = FxMatrix.builder()
        .addRate(GBP, USD, 1.7)
        .addRate(EUR, USD, 1.5)
        .addRate(EUR, CHF, 1.3)
        .build();

    FxMatrix result = matrix1.merge(matrix2);
    assertEquals(result, matrix1);
  }

  public void mergeAddsInAdditionalCurrencies() {
    FxMatrix matrix1 = FxMatrix.builder()
        .addRate(GBP, USD, 1.6)
        .addRate(EUR, USD, 1.4)
        .build();

    FxMatrix matrix2 = FxMatrix.builder()
        .addRate(EUR, CHF, 1.2)
        .addRate(CHF, AUD, 1.2)
        .build();

    FxMatrix result = matrix1.merge(matrix2);
    assertEquals(result.getCurrencies().size(), 5);

    assertEquals(result.getRate(GBP, USD), 1.6);
    assertEquals(result.getRate(GBP, EUR), 1.6 / 1.4, TOLERANCE);

    assertEquals(result.getRate(EUR, CHF), 1.2);
    assertEquals(result.getRate(CHF, AUD), 1.2);

    assertEquals(result.getRate(GBP, CHF), (1.6 / 1.4) * 1.2, TOLERANCE);
    assertEquals(result.getRate(GBP, AUD), (1.6 / 1.4) * 1.2 * 1.2, TOLERANCE);
  }

  private Pair<Currency, Currency> currencyPair(Currency ccy1, Currency ccy2) {
    return Pair.of(ccy1, ccy2);
  }

}