/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.simm;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.opengamma.simm.basics.AssetClass;
import com.opengamma.simm.format.CsvOutputFormatter;
import com.opengamma.simm.format.OutputFormatter;
import com.opengamma.simm.format.PrettyPrintOutputFormatter;
import com.opengamma.simm.load.PropertyFileLoader;
import com.opengamma.simm.util.Pair;

/**
 * Entry point to the SIMM calculation when running from
 * the command line.
 *
 * -pl - output P&amp;L vectors, otherwise just summary data
 * -o  - write output to named file
 */
public class Simm {

  public static void main(String[] args) throws IOException {

    if (args.length == 0) {
      System.err.println("Usage:");
      System.err.println("java Simm <configFileLocation> -pl -o <outputFile>");
      System.err.println();
      System.err.println("<configFileLocation> - mandatory, location of config file");
      System.err.println("-pl                  - optional, output P&L vectors rather than summary data");
      System.err.println("-o <outputFile>      - optional, write the output to the specified file rather than screen");
      return;
    }

    String configFileLocation = args[0];
    File configFile = new File(configFileLocation);

    if (!configFile.exists()) {
      System.err.println("Unable to find file at: " + configFile.getAbsolutePath());
      return;
    }

    Optional<String> outputFile = IntStream.range(0, args.length)
        .filter(i -> args[i].equals("-o"))
        .mapToObj(i -> args[i + 1])
        .findFirst();

    PropertyFileLoader propertyFileLoader = parseConfigFile(configFile);

    // If we're outputting to file, use a CSV format else pretty print on screen
    OutputFormatter formatter =
        outputFile.<OutputFormatter>map(CsvOutputFormatter::new)
            .orElse(new PrettyPrintOutputFormatter());

    boolean detail = Stream.of(args).filter(s -> s.equals("-pl")).findFirst().isPresent();
    if (detail) {
      populateFormatterWithPnlOutput(formatter, propertyFileLoader);
    }
    else {
      populateFormmatterWithStandardOutput(formatter, propertyFileLoader);
    }

    formatter.print();
  }

  private static void populateFormmatterWithStandardOutput(OutputFormatter formatter, PropertyFileLoader propertyFileLoader) {
    NumberFormat format = NumberFormat.getNumberInstance();
    format.setMaximumFractionDigits(4);
    format.setMinimumFractionDigits(4);

    Map<AssetClass, Double> var = propertyFileLoader.calculateVar();

    formatter.addHeader("Asset Class", "Var");
    formatter.addRows(var.entrySet().stream().map(e -> Arrays.asList(e.getKey(), e.getValue())));
    formatter.addFooter("Total", var.values().stream().mapToDouble(d -> d).sum());
  }

  private static void populateFormatterWithPnlOutput(OutputFormatter formatter, PropertyFileLoader propertyFileLoader) {
    Map<AssetClass, List<Pair<Integer, Double>>> pnlVectors =
        propertyFileLoader.calculatePnlVectors();

    formatter.addHeader("Asset Class", "Index", "P&L Vector");
    formatter.addRows(
        Stream.of(AssetClass.values())
            .flatMap(ac -> pnlVectors.getOrDefault(ac, new ArrayList<>())
                .stream()
                .map(p -> Arrays.asList(ac, p.getFirst(), p.getSecond()))));
  }

  private static PropertyFileLoader parseConfigFile(File configFile) throws IOException {

    Properties props = new Properties();
    try (FileReader reader = new FileReader(configFile)) {
      props.load(reader);
      return new PropertyFileLoader(props);
    }
  }

}
