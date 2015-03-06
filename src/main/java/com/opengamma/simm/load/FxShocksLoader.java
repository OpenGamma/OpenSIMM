/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.simm.load;

import com.opengamma.simm.util.ArgChecker;
import com.opengamma.simm.util.Pair;

import java.io.File;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.opengamma.simm.util.CollectionUtils.pairsToMap;
import static java.util.stream.Collectors.toList;

/**
 * Loads FX shocks from a file into a map.
 */
public class FxShocksLoader {

  private static final Pattern CURRENCY_PAIR_FORMAT = Pattern.compile("([A-Z]{3})[/]([A-Z]{3})");

  private static final List<String> EXPECTED_HEADER = Arrays.asList("CurrencyPair", "Shocks");

  private final File file;

  /**
   * Create a loader for the specified file.
   *
   * @param f  the file containing the FX shocks
   * @return a new loader
   */
  public static FxShocksLoader of(File f) {
    return new FxShocksLoader(f);
  }

  /**
   * Load the FX shocks into a Map.
   *
   * @return a Map from currency pair to shocks
   */
  public Map<Pair<Currency, Currency>, List<Double>> load() {
    return BasicCsvParser.parseFile(file, EXPECTED_HEADER, data ->
        checkShockLengths(
            data.map(this::convertToPairs)
                .collect(pairsToMap())));
  }

  private Map<Pair<Currency, Currency>, List<Double>> checkShockLengths(
      Map<Pair<Currency, Currency>, List<Double>> initialResult) {

    long shockVariations = initialResult.values()
        .stream()
        .map(List::size)
        .distinct()
        .count();

    // All shocks were the same length or there was no data
    if (shockVariations <= 1) {
      return initialResult;
    } else {
      throw new IllegalArgumentException("All shocks must be the same length");
    }
  }

  private Pair<Pair<Currency, Currency>, List<Double>> convertToPairs(List<String> row) {

    Pair<Currency, Currency> currencyPair = extractCurrencyPair(row.get(0));

    if (row.size() > 1) {
      List<Double> shocks =
          row.stream()
              .skip(1)  // Skip the currency pair which was already handled
              .map(Double::valueOf)
              .collect(toList());

      return Pair.of(currencyPair, shocks);
    } else {
      throw new IllegalArgumentException("No shocks found for currency pair: " + currencyPair);
    }
  }

  private Pair<Currency, Currency> extractCurrencyPair(String s) {
    Matcher matcher = CURRENCY_PAIR_FORMAT.matcher(s.toUpperCase(Locale.ENGLISH));
    if (matcher.matches()) {
      return Pair.of(Currency.getInstance(matcher.group(1)), Currency.getInstance(matcher.group(2)));
    } else {
      throw new IllegalArgumentException("Invalid currency pair: " + s);
    }
  }

  private FxShocksLoader(File file) {
    checkFile(file);
    this.file = file;
  }

  private void checkFile(File file) {
    ArgChecker.notNull(file, "file");
    ArgChecker.isTrue(file.exists(), "File: {} could not be found", file);
  }

}
