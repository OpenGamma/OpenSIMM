/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.simm.load;

import com.opengamma.simm.basics.StandardRiskFactor;
import com.opengamma.simm.basics.RiskFactor;
import com.opengamma.simm.util.ArgChecker;
import com.opengamma.simm.util.Pair;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.opengamma.simm.util.CollectionUtils.pairsToMap;
import static java.util.stream.Collectors.toList;

/**
 * Loads risk factor shocks from a file, producing
 * a {@code Map<RiskFactor, List<Double>>}.
 */
public class RiskFactorShocksLoader {

  private static final List<String> EXPECTED_HEADER = Arrays.asList("RiskFactorName", "Shocks");

  private final File file;

  /**
   * Create a loader for the specified file.
   *
   * @param f  the file containing the risk factor shocks
   * @return a new loader
   */
  public static RiskFactorShocksLoader of(File f) {
    return new RiskFactorShocksLoader(f);
  }

  /**
   * Load the risk factor shocks into a {@code Map<RiskFactor, List<Double>>}.
   *
   * @return a map containing the risk factor shocks
   */
  public Map<RiskFactor, List<Double>> load() {
    return BasicCsvParser.parseFile(
        file,
        EXPECTED_HEADER,
        data -> data.map(this::convertToPairs).collect(pairsToMap()));
  }

  private Pair<RiskFactor, List<Double>> convertToPairs(List<String> row) {

    RiskFactor name = StandardRiskFactor.of(row.get(0));

    if (row.size() > 1) {
      List<Double> shocks =
          row.stream()
              .skip(1)  // Skip the name which was already handled
              .map(Double::valueOf)
              .collect(toList());

      return Pair.of(name, shocks);
    } else {
      throw new IllegalArgumentException("No shocks found for risk factor: " + name);
    }
  }

  private RiskFactorShocksLoader(File file) {
    checkFile(file);
    this.file = file;
  }

  private void checkFile(File file) {
    ArgChecker.notNull(file, "file");
    ArgChecker.isTrue(file.exists(), "File: {} could not be found", file);
  }

}
