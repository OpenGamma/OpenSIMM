/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.simm.basics;

import static com.opengamma.simm.util.CollectionUtils.entriesToMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.opengamma.simm.util.ArgChecker;
import com.opengamma.simm.util.Pair;

/**
 * Immutable class describing a set of currencies and all the cross rates between them.
 */
public class FxMatrix {

  public static final FxMatrix EMPTY_FX_MATRIX = builder().build();

  /**
   * The map between the currencies and their order. A LinkedHashMap is
   * used so that the currencies are correctly ordered when the
   * {@link #toString()} method is called. Using a Guava immutable Map
   * would be a better approach in the future.
   */
  private final LinkedHashMap<Currency, Integer> currencies;

  /**
   * The matrix with all exchange rates. The entry [i][j] is such that
   * 1.0 * Currency[i] = _fxrate * Currency[j]. If _currencies.get(EUR) = 0 and
   * _currencies.get(USD) = 1, the element _fxRate[0][1] is likely to be something
   * like 1.40 and _fxRate[1][0] like 0.7142... The rate _fxRate[1][0] will be
   * computed from _fxRate[0][1] when the object is constructed. All the elements
   * of the matrix are meaningful and coherent.
   */
  private final double[][] rates;

  /**
   * Private constructor.
   */
  private FxMatrix(LinkedHashMap<Currency, Integer> currencies, double[][] rates) {
    this.currencies = currencies;
    this.rates = rates;
  }

  /**
   * Create a new FxMatrix builder.
   *
   * @return a new FxMatrix builder
   */
  public static FxMatrix.Builder builder() {
    return new FxMatrix.Builder();
  }

  /**
   * Create a new builder using the data from this matrix to
   * create a set of initial entries.
   *
   * @return a new builder containing the data from this matrix
   */
  public FxMatrix.Builder toBuilder() {
    return new FxMatrix.Builder(currencies, rates);
  }

  /**
   * Return the exchange rate between two currencies.
   *
   * @param ccy1 The first currency.
   * @param ccy2 The second currency.
   * @return The exchange rate: 1.0 * ccy1 = x * ccy2.
   */
  public double getRate(Currency ccy1, Currency ccy2) {
    if (ccy1.equals(ccy2)) {
      return 1;
    }
    Integer index1 = currencies.get(ccy1);
    Integer index2 = currencies.get(ccy2);
    if (index1 != null && index2 != null) {
      return rates[index1][index2];
    } else {
      throw new IllegalArgumentException(
          "No rate found for " + ccy1 + "/" + ccy2 +
          " - FX matrix only contains rates for: " + currencies.keySet());
    }
  }

  /**
   * Merge the entries from the other matrix into this one. The other matrix
   * should have at least one currency in common with this one.
   * The additional currencies from the other matrix are added one by one
   * and the exchange rate data created is coherent with some data in this
   * matrix.
   * <p>
   * Note that if the other matrix has more than one currency in common with
   * this one, and the rates for pairs of those currencies are different to
   * the equivalents in this matrix, then the rates between the additional
   * currencies is this matrix will differ from those in the original.
   *
   * @param other  the matrix to be merged into this one
   * @return a new matrix containing the rates from this matrix
   *   plus any rates for additional currencies from the other matrix
   */
  public FxMatrix merge(FxMatrix other) {
    return toBuilder().merge(other.toBuilder()).build();
  }
  
  /**
   * Returns an unmodifiable copy of the map containing currency and order information.
   * @return The currency and order information
   */
  public Set<Currency> getCurrencies() {
    return Collections.unmodifiableSet(currencies.keySet());
  }

  @Override
  public String toString() {
    return getCurrencies() + " - " + Stream.of(rates).map(Arrays::toString).collect(Collectors.joining());
  }

  @Override
  public int hashCode() {
    return 31 * currencies.hashCode() + Arrays.hashCode(rates);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    FxMatrix other = (FxMatrix) obj;
    return currencies.equals(other.currencies) && Arrays.deepEquals(rates, other.rates);
  }

  /**
   * Creates a {@link Collector} that allows a Map of currency pair to rates
   * to be streamed and collected into a new {@code FxMatrix}.
   *
   * @return a collector for creating an {@code FxMatrix} from a stream
   */
  public static Collector<? super Pair<Pair<Currency, Currency>, Double>, Builder, FxMatrix> pairCollector() {
    return collector((builder, pair) ->
        builder.addRate(pair.getFirst().getFirst(), pair.getFirst().getSecond(), pair.getSecond()));
  }

  /**
   * Creates a {@link Collector} that allows a collection of pairs each containing
   * a currency pair and a rate to be streamed and collected into a new {@code FxMatrix}.
   *
   * @return a collector for creating an {@code FxMatrix} from a stream
   */
  public static Collector<? super Map.Entry<Pair<Currency, Currency>, Double>, Builder, FxMatrix> entryCollector() {
    return collector((builder, entry) ->
        builder.addRate(entry.getKey().getFirst(), entry.getKey().getSecond(), entry.getValue()));
  }

  private static <T> Collector<T, Builder, FxMatrix> collector(BiConsumer<Builder, T> accumulator) {
    return Collector.of(
        FxMatrix::builder,
        accumulator,
        Builder::merge,
        Builder::build);
  }

  /**
   * Builder class for FxMatrix. Can be created either by the static
   * {@link FxMatrix#builder()} or from an existing {@code FxMatrix}
   * instance by calling {@link FxMatrix#toBuilder()}.
   */
  public static final class Builder {

    /**
     * The minimum size of the FX rate matrix. This is intended such
     * that a number rates of can be added without needing to resize.
     */
    private static final int MINIMAL_MATRIX_SIZE = 8;

    /**
     * The currencies held by the builder pointing to their position
     * in the rates array. An ordered map is used so that when
     */
    private final LinkedHashMap<Currency, Integer> currencies;

    /**
     * A 2 dimensional array holding the rates. Each row of the array holds the
     * value of 1 unit of Currency (that the row represents) in each of the
     * alternate currencies.
     *
     * The array is square with its order being a power of 2. This means that there
     * may be empty rows/cols at the bottom/right of the matrix. Leaving this space
     * means that adding currencies can be done more efficiently as the array only
     * needs to be resized (via copying) relatively infrequently..
     */
    private double[][] rates;

    /**
     * Build a new {@code FxMatrix} from the data in the builder.
     *
     * @return a new {@code FxMatrix}
     */
    public FxMatrix build() {
      // Trim array down to the correct size - we have to copy the array
      // anyway to ensure immutability, so we may as well remove any
      // unused rows
      return new FxMatrix(new LinkedHashMap<>(currencies), copyArray(rates, currencies.size()));
    }

    private Builder() {
      this.currencies = new LinkedHashMap<>();
      this.rates = new double[MINIMAL_MATRIX_SIZE][MINIMAL_MATRIX_SIZE];
    }

    public Builder merge(Builder other) {

      // Find the common currencies
      Optional<Currency> common = currencies.keySet()
          .stream()
          .filter(other.currencies::containsKey)
          .findFirst();

      if (!common.isPresent()) {
        throw new IllegalArgumentException("There are no currencies in common between " +
            currencies.keySet() + " and " + other.currencies.keySet());
      }

      Currency commonCurrency = common.get();

      // Add in all currencies that we don't already have
      other.currencies.entrySet()
          .stream()
          .filter(e -> !e.getKey().equals(commonCurrency) && !currencies.containsKey(e.getKey()))
          .forEach(e -> addCurrencyPair(commonCurrency, e.getKey(), other.getRate(commonCurrency, e.getKey())));

      return this;
    }

    private double getRate(Currency ccy1, Currency ccy2) {
      int i = currencies.get(ccy1);
      int j = currencies.get(ccy2);
      return rates[i][j];
    }

    public Builder addRate(Currency ccy1, Currency ccy2, double rate) {

      if (currencies.isEmpty()) {
        addInitialCurrencyPair(ccy1, ccy2, rate);
      } else {
        addCurrencyPair(ccy1, ccy2, rate);
      }
      return this;
    }

    private void addCurrencyPair(Currency ccy1, Currency ccy2, double rate) {
      ArgChecker.isTrue(currencies.containsKey(ccy1) || currencies.containsKey(ccy2),
          "One of the currencies: [{}, {}] must already be in the matrix but it only contains: {}",
          ccy1, ccy2, currencies.keySet());
      ensureCapacity(Stream.of(ccy1, ccy2));

      Currency existing = currencies.containsKey(ccy1) ? ccy1 : ccy2;
      Currency other = existing == ccy1 ? ccy2 : ccy1;

      double updatedRate = existing == ccy2 ? rate : 1.0 / rate;
      int indexRef = currencies.get(existing);
      int indexOther;

      if (currencies.containsKey(other)) {
        // Update to an existing rate
        indexOther = currencies.get(other);
      } else {
        // Adding a new rate
        indexOther = currencies.size();
        currencies.put(other, indexOther);
        rates[indexOther][indexOther] = 1.0;
      }

      for (int i = 0; i < indexOther; i++) {
        double convertedRate = updatedRate * rates[indexRef][i];
        rates[indexOther][i] = convertedRate;
        rates[i][indexOther] = 1.0 / convertedRate;
      }
    }

    private void addInitialCurrencyPair(Currency ccy1, Currency ccy2, double rate) {
      // No need for capacity check, as initial size is always enough
      currencies.put(ccy1, 0);
      currencies.put(ccy2, 1);
      rates[0][0] = 1.0;
      rates[0][1] = rate;
      rates[1][1] = 1.0;
      rates[1][0] = 1.0 / rate;
    }

    public Builder addRates(Map<Pair<Currency, Currency>, Double> rates) {

      if (!rates.isEmpty()) {

        ensureCapacity(
            rates.keySet()
                .stream()
                .flatMap(pair ->
                    Stream.<Currency>of(pair.getFirst(), pair.getSecond())));

        if (currencies.isEmpty()) {
          Map.Entry<Pair<Currency, Currency>, Double> first = rates.entrySet().iterator().next();
          Pair<Currency, Currency> currencyPair = first.getKey();
          addRate(currencyPair.getFirst(), currencyPair.getSecond(), first.getValue());
        }

        orderRates(rates, currencies.keySet())
            .entrySet()
            .stream()
            .forEach(e ->
                addRate(e.getKey().getFirst(), e.getKey().getSecond(), e.getValue()));
      }

      return this;
    }

    // When adding multiple rates some entries may have a currency pair
    // containing currencies we have not seen so they cannot be added
    // until we have a rate for one of them. This method reorders the
    // rates so that they can be added.
    private LinkedHashMap<Pair<Currency, Currency>, Double> orderRates(
        Map<Pair<Currency, Currency>, Double> rates,
        Set<Currency> currencies) {

      Set<Currency> seen = new HashSet<>(currencies);
      LinkedHashMap<Pair<Currency, Currency>, Double> ordered = new LinkedHashMap<>();
      Map<Pair<Currency, Currency>, Double> remaining = new HashMap<>(rates);

      while (!remaining.isEmpty()) {
        Map<Pair<Currency, Currency>, Double> valid =
            remaining.entrySet()
              .stream()
              .filter(e ->
                  seen.contains(e.getKey().getFirst()) || seen.contains(e.getKey().getSecond()))
              .collect(entriesToMap());

        if (valid.isEmpty()) {
          throw new IllegalArgumentException(
              "Received collection of rates containing disjoint sets of currency pairs. " +
              "Original currencies: " + currencies + "usable currencies: " + ordered +
              ", disjoint currencies: " + remaining);
        }

        ordered.putAll(valid);
        valid.keySet()
            .stream()
            .forEach(cp -> {
              remaining.remove(cp);
              seen.add(cp.getFirst());
              seen.add(cp.getSecond());
            });
      }
      return ordered;
    }

    private void ensureCapacity(Stream<Currency> potentialCurrencies) {
      // If adding the currencies would mean we have more
      // currencies than matrix size, create an expanded array
      int requiredOrder =
          (int) Stream.concat(currencies.keySet().stream(), potentialCurrencies)
              .distinct()
              .count();

      ensureCapacity(requiredOrder);
    }

    private void ensureCapacity(int requiredOrder) {
      if (requiredOrder > rates.length) {
        rates = copyArray(rates, size(requiredOrder));
      }
    }

    private Builder(Map<Currency, Integer> currencies, double[][] rates) {
      this.currencies = new LinkedHashMap<>(currencies);
      // Ensure there is space to add at least one new currency
      this.rates = copyArray(rates, size(currencies.size() + 1));
    }

    private int size(int requiredCapacity) {
      int lowerPower = Integer.highestOneBit(requiredCapacity);
      return Math.max(requiredCapacity == lowerPower ? requiredCapacity : lowerPower << 2, MINIMAL_MATRIX_SIZE);
    }

    private double[][] copyArray(double[][] rates, int requestedSize) {
      int order = Math.min(rates.length, requestedSize);
      double[][] copy = new double[requestedSize][requestedSize];
      for (int i = 0; i < order; i++) {
        System.arraycopy(rates[i], 0, copy[i], 0, order);
      }
      return copy;
    }
  }
}

