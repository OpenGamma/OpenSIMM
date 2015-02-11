/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.simm.computing;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import com.opengamma.simm.basics.RiskFactor;
import com.opengamma.simm.utils.ArgChecker;

/**
 * Utilities used to compute the SIMM-like VaR figures.
 */
public class SimmUtils {

  /**
   * Compute the value at a specified percentile from set of values.
   * From the values the discrete percentiles are computed. The percentile
   * for the level requested is estimated by linear interpolation on the
   * cumulative distribution function.
   *
   * @param values  the (unsorted) array of values
   * @param level  the level at which the percentile should be computed
   * @return the percentile value
   */
  public static double percentile(List<Double> values, double level) {

    int size = values.size();
    ArgChecker.isTrue(level < 1.0d - 0.5d / size, "level not within the data range");

    List<Double> sorted = values.stream()
        .sorted(Double::compare)
        .collect(toList());

    int i = (int) Math.ceil(size * level - 0.5);
    double lower = (i - 0.5) / size;
    double upper = (i + 0.5) / size;
    double lowerValue = sorted.get(i - 1);
    double upperValue = sorted.get(i);
    return lowerValue + (level - lower) * (upperValue - lowerValue) / (upper - lower);
  }

  /**
   * Computes the profits from the market movements and the portfolio exposures.
   *
   * @param marketMovements  the market movements
   * @param exposure  the portfolio exposure to the risk factors
   * @return the profit series
   */
  public static List<Double> profits(
      Map<RiskFactor, List<Double>> marketMovements,
      Map<RiskFactor, Double> exposure) {

    int shocksSize = marketMovements.values()
        .stream()
        .map(List::size)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Market movement shocks are empty"));

    return marketMovements.entrySet()
        .stream()
        // Ignore movements where we have no exposure
        .filter(e -> exposure.getOrDefault(e.getKey(), 0d) != 0d)
        // Calculate contribution to profits from each risk factor
        .map(e -> calculateProfits(e.getValue(), exposure.get(e.getKey())))
        // Sum the lists of doubles creating a final list of the same size
        .reduce((l1, l2) ->
            IntStream.range(0, l1.size())
                .mapToObj(i -> l1.get(i) + l2.get(i))
                .collect(toList()))
        .orElse(createZeroList(shocksSize));
  }

  private static List<Double> createZeroList(int shocksSize) {
    return DoubleStream.of(new double[shocksSize]).mapToObj(d -> d).collect(toList());
  }

  private static List<Double> calculateProfits(List<Double> movements, double delta) {
    return movements
        .stream()
        .map(d -> d * delta)
        .collect(toList());
  }

  // Private constructor for utils class
  private SimmUtils() {
  }

}
