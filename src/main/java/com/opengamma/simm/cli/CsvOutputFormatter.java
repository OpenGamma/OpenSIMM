/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.simm.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * A formatter which will output data to a file in simple CSV format.
 */
public class CsvOutputFormatter implements OutputFormatter {

  private final List<List<Object>> headers = new ArrayList<>();
  private final List<List<Object>> footers = new ArrayList<>();
  private final Function<List<Object>, String> formatter = row -> row.stream().map(Object::toString).collect(joining(","));
  private final File file;
  private Stream<List<Object>> rowStream;

  /**
   * Create the formatter for the specified file. If the file
   * does not exist then it will be created. If it does exist
   * then it will be overwritten.
   *
   * @param fileName  name of the file to output data to
   */
  public CsvOutputFormatter(String fileName) {
    this.file = new File(fileName);
  }

  @Override
  public void addHeader(Object... header) {
    headers.add(Arrays.asList(header));
  }

  @Override
  public void addRows(Stream<List<Object>> rows) {
    rowStream = rows;
  }

  @Override
  public void addFooter(Object... footer) {
    footers.add(Arrays.asList(footer));
  }

  @Override
  public void print() {

    try (PrintStream printStream = new PrintStream(file)) {
      System.out.println("Writing data to file: " + file.getAbsolutePath());

      formatAndOutput(headers.stream(), printStream);
      formatAndOutput(rowStream, printStream);
      formatAndOutput(footers.stream(), printStream);
    } catch (FileNotFoundException e) {
      throw new IllegalStateException("Exception whilst writing file", e);
    }
  }

  private void formatAndOutput(Stream<List<Object>> outputRows, PrintStream printStream) {
    outputRows.map(formatter).forEach(printStream::println);
  }
}
