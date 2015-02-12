/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

/**
 * Initial implementation of the Standard Initial Margin Model (SIMM)
 * approach described by ISDA.
 * <p>
 * Methods do not accept or return null values. Any exceptions to this
 * will be noted in the accompanying javadoc.
 * <p>
 * There are two classes in this package which provides entry points
 * for SIMM calculation:
 * <ul>
 *   <li>{@link SimmCalculator} (and the contained
 *   {@link com.opengamma.simm.SimmCalculator.SimmCalculatorBuilder})
 *   contain methods for initialization and then executes the SIMM
 *   calculation</li>
 *   <li>{@link Simm} has a main method and can be run from the command line to
 *   execute a SIMM calculation based on data from a set of files</li>
 * </ul>
 * For more information about usage of both these classes, including code
 * samples, please see the README file in the root directory.
 */
package com.opengamma.simm;
