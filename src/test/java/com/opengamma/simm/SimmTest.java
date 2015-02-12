/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.simm;

import static com.opengamma.simm.basics.AssetClass.COMMODITY;
import static com.opengamma.simm.basics.AssetClass.CREDIT;
import static com.opengamma.simm.basics.AssetClass.EQUITY;
import static com.opengamma.simm.basics.AssetClass.INTEREST_RATE;
import static com.opengamma.simm.util.CollectionUtils.createMap;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.testng.annotations.Test;

import com.opengamma.simm.basics.AssetClass;
import com.opengamma.simm.util.Pair;

/**
 * Test the main entry point behaves as expected.
 */
@Test
public class SimmTest {

  public void canLoadFromPropertiesFile() throws IOException {
    Simm.main(new String[]{"src/test/resources/simm-sample/simm.properties"});
  }

  public void canWriteResultsToFile() throws IOException {

    File tempFile = Files.createTempFile("simple", ".csv").toFile();
    Simm.main(new String[]{"src/test/resources/simm-sample/simm.properties", "-o", tempFile.getAbsolutePath()});

    assertTrue(tempFile.exists());
    assertEquals(streamFile(tempFile).count(), 6);
    List<String> lines = streamFile(tempFile).collect(toList());

    assertEquals(lines.get(0), "Asset Class,Var");

    // Use regex as csv double values are not truncated
    assertTrue(lines.get(5).matches("Total,1785.949\\d*"));

    Map<AssetClass, Double> expected = createMap(
        COMMODITY, 564.3703,
        CREDIT, 33.3300,
        EQUITY, 740.7143,
        INTEREST_RATE, 447.5351);

    lines.stream()
        .filter(s -> !s.matches("Asset.*|Total.*"))
        .map(s -> Pair.of(AssetClass.valueOf(s.split(",")[0]), Double.parseDouble(s.split(",")[1])))
        .forEach(p -> assertEquals(p.getSecond(), expected.get(p.getFirst()), 1e-4));
  }

  public void canWritePnlResultsToFile() throws IOException {

    File tempFile = Files.createTempFile("pnl", ".csv").toFile();
    Simm.main(new String[]{"src/test/resources/simm-sample/simm.properties", "-pl", "-o", tempFile.getAbsolutePath()});

    assertTrue(tempFile.exists());
    assertEquals(streamFile(tempFile).count(), 67);
    List<String> lines = streamFile(tempFile).collect(toList());
    assertEquals(lines.get(0), "Asset Class,Index,P&L Vector");
  }

  private Stream<String> streamFile(File tempFile) throws IOException {
    return Files.lines(tempFile.toPath());
  }
}