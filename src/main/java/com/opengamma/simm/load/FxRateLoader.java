/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.simm.load;

import java.io.File;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.opengamma.simm.basics.FxMatrix;
import com.opengamma.simm.util.ArgChecker;
import com.opengamma.simm.util.Pair;

/**
 * Loads base FX rates from a file, producing an {@link FxMatrix}.
 */
public class FxRateLoader {

  private static final Pattern CURRENCY_PAIR_FORMAT = Pattern.compile("([A-Z]{3})[/]([A-Z]{3})");

  private static final List<String> EXPECTED_HEADER = Arrays.asList("CurrencyPair", "Rate");

  private final File file;

  /**
   * Create a loader for the specified file.
   *
   * @param f  the file containing the base FX rates
   * @return a new loader
   */
  public static FxRateLoader of(File f) {
    return new FxRateLoader(f);
  }

  /**
   * Load the FX rates into an FX Matrix.
   *
   * @return an FX matrix containing the rates from the file
   */
  public FxMatrix load() {
    return BasicCsvParser.parseFile(file, EXPECTED_HEADER, data ->
        data.map(this::convertToPairs)
            .collect(FxMatrix.pairCollector()));
  }

  private Pair<Pair<Currency, Currency>, Double> convertToPairs(List<String> row) {
    return Pair.of(extractCurrencyPair(row.get(0)), Double.parseDouble(row.get(1)));
  }

  private Pair<Currency, Currency> extractCurrencyPair(String s) {
    Matcher matcher = CURRENCY_PAIR_FORMAT.matcher(s.toUpperCase(Locale.ENGLISH));
    if (matcher.matches()) {
      return Pair.of(Currency.getInstance(matcher.group(1)), Currency.getInstance(matcher.group(2)));
    } else {
      throw new IllegalArgumentException("Invalid currency pair: " + s);
    }
  }

  private FxRateLoader(File file) {
    checkFile(file);
    this.file = file;
  }

  private void checkFile(File file) {
    ArgChecker.notNull(file, "file");
    ArgChecker.isTrue(file.exists(), "File: {} could not be found", file);
  }

}
