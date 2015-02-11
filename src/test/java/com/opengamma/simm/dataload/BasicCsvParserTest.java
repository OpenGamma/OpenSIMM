/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.simm.dataload;

import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;
import static com.opengamma.simm.utils.CollectionUtils.pairsToMap;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.simm.utils.Pair;

@Test
public class BasicCsvParserTest {

  private static final String BASE_DIR = "src/test/resources/parser-test/basic/";

  private static final List<String> EXPECTED_HEADER = Arrays.asList("Header1", "Another Header", "One More");

  /**
   * Load a file and check it's as expected
   */
  public void simpleLoadHasExpectedRawData() {

    File file = new File(BASE_DIR + "simple.csv");

    List<List<String>> result = BasicCsvParser.parseFile(file, EXPECTED_HEADER,
        data -> data.collect(toList()));

    assertEquals(result.get(0), Arrays.asList("row1", "data 11", "data 12"));
    assertEquals(result.get(1), Arrays.asList("row2", "data 21", "data 22"));
    assertEquals(result.get(2), Arrays.asList("row3", "data 31", "data 32"));

  }

  public void blankLinesAreSkipped() {

    File file = new File(BASE_DIR + "simple-with-empty-lines.csv");

    List<List<String>> result = BasicCsvParser.parseFile(file, EXPECTED_HEADER,
        data -> data.collect(toList()));

    assertEquals(result.get(0), Arrays.asList("row1", "data 11", "data 12"));
    assertEquals(result.get(1), Arrays.asList("row2", "data 21", "data 22"));
    assertEquals(result.get(2), Arrays.asList("row3", "data 31", "data 32"));
  }

  public void simpleLoadWithTransformation() {

    File file = new File(BASE_DIR + "simple.csv");

    Map<Integer, Double> result = BasicCsvParser.parseFile(file, EXPECTED_HEADER, data ->
        data.map(l -> Pair.of(
                  Integer.parseInt(l.get(0).substring(3)),
                  Double.parseDouble(l.get(1).substring(5)) / Double.parseDouble(l.get(2).substring(5))))
                .collect(pairsToMap()));

    assertEquals(result.get(1), 11d / 12);
    assertEquals(result.get(2), 21d / 22);
    assertEquals(result.get(3), 31d / 32);
  }

}