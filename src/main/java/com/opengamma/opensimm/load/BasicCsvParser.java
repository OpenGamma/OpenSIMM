/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.opensimm.load;

import static java.util.stream.Collectors.toList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import com.opengamma.opensimm.util.ArgChecker;

/**
 * Basic CSV parser handling simple cases only.
 * <p>
 * Specifically:
 * <ul>
 *   <li>the file must contain a header row</li>
 *   <li>the parser does not handle quoting of data</li>
 *   <li>the parser does not handle escaping of characters</li>
 * </ul>
 */
public class BasicCsvParser {

  /**
   * Parse the specified file using the supplied handler, ensuring
   * the header of the file matches expectations.
   *
   * @param file  the file to be parsed
   * @param expectedHeader  the header row the file is expected to
   *   contain. This provides a basic check that the correct type
   *   of file is being processed.
   * @param handler  handler taking the rows from the file as
   *   a stream and combining them into an object. Each row is
   *   supplied to the handler as a {@code List<String>}.
   * @param <T>  the type of data to be returned on a successful parse
   * @return a result object of the expected type
   * @throws RuntimeException if there are problems reading the file
   */
  public static <T> T parseFile(
      File file,
      List<String> expectedHeader,
      Function<Stream<List<String>>, T> handler) {

    try(BufferedReader reader = createReader(file)) {

      // Read the first line to use as header
      List<String> header = splitLine(reader.readLine());

      ArgChecker.isTrue(expectedHeader.equals(header),
          "Expected header to contain: {} but was: {}",  expectedHeader, header);

      // Now create a stream for the rest of the file
      Stream<List<String>> data =
          reader.lines()
              .filter(l -> !l.isEmpty())
              .map(BasicCsvParser::splitLine);

      return handler.apply(data);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static BufferedReader createReader(File file) throws FileNotFoundException {
    return new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
  }

  private static List<String> splitLine(String line) {
    return Stream.of(line.split(",")).map(String::trim).collect(toList());
  }
}
