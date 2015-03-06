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

/**
 * Loads risk factor base levels from a file, producing a {@code Map<RiskFactor, Double>}.
 */
public class RiskFactorBaseLevelsLoader {

  private static final List<String> EXPECTED_HEADER = Arrays.asList("RiskFactorName", "BaseLevel");

  private final File file;

  /**
   * Create a loader for the specified file.
   *
   * @param f  the file containing the risk factor base levels
   * @return a new loader
   */
  public static RiskFactorBaseLevelsLoader of(File f) {
    return new RiskFactorBaseLevelsLoader(f);
  }

  /**
   * Load the base levels into a Map.
   *
   * @return a map containing RiskFactor to Double
   */
  public Map<RiskFactor, Double> load() {
    return BasicCsvParser.parseFile(file, EXPECTED_HEADER, data ->
        data.map(this::convertToPairs)
            .collect(pairsToMap()));
  }

  private Pair<RiskFactor, Double> convertToPairs(List<String> row) {
    return Pair.of(StandardRiskFactor.of(row.get(0)), Double.parseDouble(row.get(1)));
  }

  private RiskFactorBaseLevelsLoader(File file) {
    checkFile(file);
    this.file = file;
  }

  private void checkFile(File file) {
    ArgChecker.notNull(file, "file");
    ArgChecker.isTrue(file.exists(), "File: {} could not be found", file);
  }

}
