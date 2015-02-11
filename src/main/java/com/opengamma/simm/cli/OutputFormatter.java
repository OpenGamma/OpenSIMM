/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.simm.cli;

import java.util.List;
import java.util.stream.Stream;

/**
 * A simple formatter for the output of the command line interface.
 * <p>
 * Data can be added to the formatter using the {@link #addHeader(Object...)},
 * {@link #addRows(java.util.stream.Stream)} and {@link #addFooter(Object...)}
 * methods. When complete, calling {@link #print()} will format the data
 * and output it.
 */
public interface OutputFormatter {

  /**
   * Add a header to the data to be output.
   *
   * @param header the header to be added
   */
  public abstract void addHeader(Object... header);

  /**
   * Add data rows to the output.
   *
   * @param rows the rows to be added.
   */
  public abstract void addRows(Stream<List<Object>> rows);

  /**
   * Add a footer to the data to be output.
   *
   * @param footer the footer to be added
   */
  public abstract void addFooter(Object... footer);

  /**
   * Format and print the data contained in the formatter.
   */
  public abstract void print();

}
