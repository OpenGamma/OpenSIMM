/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.simm.format;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * A formatter which will output data to the command line
 * in a simple tabular format.
 */
public class PrettyPrintOutputFormatter implements OutputFormatter {

  private final List<List<Object>> headers = new ArrayList<>();
  private final List<List<Object>> footers = new ArrayList<>();
  private Stream<List<Object>> rowStream;

  private static final NumberFormat numberFormat = buildNumberFormatter();

  private static NumberFormat buildNumberFormatter() {
    NumberFormat nf = NumberFormat.getNumberInstance();
    nf.setMaximumFractionDigits(4);
    nf.setMinimumFractionDigits(4);
    return nf;
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

    PrintStream printStream = System.out;

    // First, format everything as Strings
    // Second, works out required widths
    // Finally format using the widths
    List<List<Object>> rows = rowStream.collect(toList());

    Stream<List<String>> allRowsAsString =
        Stream.concat(Stream.concat(rows.stream(), headers.stream()), footers.stream())
            .map(this::formatRow);

    int size = headers.get(0).size();

    List<Integer> maxWidths =
        allRowsAsString
            .map(row -> row.stream().map(String::length).collect(toList()))
            .reduce(
                IntStream.range(0, size).mapToObj(i -> 0).collect(toList()),
                (l1, l2) -> findMaxima(size, l1, l2), (l1, l2) -> l1);

    if (!headers.isEmpty()) {
      outputSeparator(maxWidths, printStream);
      formatAndOutput(headers.stream(), maxWidths, printStream);
      outputSeparator(maxWidths, printStream);
    }

    formatAndOutput(rows.stream(), maxWidths, printStream);

    if (!footers.isEmpty()) {
      outputSeparator(maxWidths, printStream);
      formatAndOutput(footers.stream(), maxWidths, printStream);
      outputSeparator(maxWidths, printStream);
    }
  }

  private void outputSeparator(List<Integer> maxWidths, PrintStream printStream) {
    printStream.println(maxWidths.stream().map(i -> repeatChar("-", i)).collect(joining(" ")));
  }

  private String repeatChar(String s, Integer i) {
    return String.join("", Collections.nCopies(i, s));
  }

  private List<Integer> findMaxima(int size, List<Integer> l1, List<Integer> l2) {
    return IntStream.range(0, size).mapToObj(i -> Math.max(l1.get(i), l2.get(i))).collect(toList());
  }

  private String formatRow(List<Object> row, List<Integer> widths) {

    return IntStream.range(0, widths.size())
        .mapToObj(i -> formatRowItem(row.get(i), widths.get(i)))
        .collect(joining(" "));
  }

  private String formatRowItem(Object o, int width) {
    return o instanceof Number ? formatNumber(o, width) : formatObject(o, width);
  }

  private String formatObject(Object o, int width) {
    String s = o.toString();
    int pad = width - s.length();
    return s + (pad > 0 ? repeatChar(" ", pad) : "");
  }

  private String formatNumber(Object o, int width) {
    String format = o instanceof Double ? numberFormat.format(o) : o.toString();

    int pad = width - format.length();
    return (pad > 0 ? repeatChar(" ", pad) : "") + format;
  }

  private List<String> formatRow(List<Object> row) {
    return row.stream().map(o -> formatRowItem(o, 0)).collect(toList());
  }

  private void formatAndOutput(Stream<List<Object>> outputRows, List<Integer> maxWidths, PrintStream printStream) {
    outputRows.map(row -> formatRow(row, maxWidths))
        .forEach(printStream::println);
  }
}
