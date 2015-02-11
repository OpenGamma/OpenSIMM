/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.simm.dataload;

import static com.opengamma.simm.utils.CollectionUtils.pairsToMap;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.opengamma.simm.basics.StandardRiskFactor;
import com.opengamma.simm.basics.RiskFactor;
import com.opengamma.simm.basics.RiskFactorProperties;
import com.opengamma.simm.basics.AssetClass;
import com.opengamma.simm.basics.RiskFactorProperties.RiskType;
import com.opengamma.simm.utils.ArgChecker;
import com.opengamma.simm.utils.Pair;

/**
 * Loads risk factor definitions from a file, producing
 * a {@code Map<RiskFactor, RiskFactorProperties>}.
 */
public class RiskFactorDefinitionsLoader {

  private static final List<String> EXPECTED_HEADER =
      Arrays.asList("RiskFactorName", "AssetClass", "RiskType", "ShockType", "Shift");

  private final File file;

  /**
   * Create a loader for the specified file.
   *
   * @param f  the file containing the risk factor definitions
   * @return a new loader
   */
  public static RiskFactorDefinitionsLoader of(File f) {
    return new RiskFactorDefinitionsLoader(f);
  }

  /**
   * Load the risk factor definitions into a map.
   *
   * @return a map containing the risk factor definitions
   */
  public Map<RiskFactor, RiskFactorProperties> load() {

    return BasicCsvParser.parseFile(file, EXPECTED_HEADER, data ->
        data.map(this::convertToPairs)
            .collect(pairsToMap()));
  }

  private Pair<RiskFactor, RiskFactorProperties> convertToPairs(List<String> row) {
    StandardRiskFactor name = StandardRiskFactor.of(row.get(0));
    RiskFactorProperties properties = extractProperties(row);
    return Pair.of(name, properties);
  }

  private RiskFactorProperties extractProperties(List<String> row) {
    AssetClass assetClass = AssetClass.valueOf(row.get(1).toUpperCase());
    RiskType riskType = RiskType.valueOf(row.get(2));

    String shiftType = row.get(3);
    switch(shiftType) {
      case "AB":
        return RiskFactorProperties.absoluteShock(assetClass, riskType);
      case "RE":
        double shift = (row.size() > 4 && !row.get(4).isEmpty()) ? Double.parseDouble(row.get(4)) : 0;
        return RiskFactorProperties.relativeShock(assetClass, riskType, shift);
      default:
        throw new IllegalArgumentException("Unknown shift type: " + shiftType);
    }
  }

  private RiskFactorDefinitionsLoader(File file) {
    checkFile(file);
    this.file = file;
  }

  private void checkFile(File file) {
    ArgChecker.notNull(file, "file");
    ArgChecker.isTrue(file.exists(), "File: {} could not be found", file);
  }
}
