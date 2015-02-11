package com.opengamma.simm.computing;

import static com.opengamma.simm.utils.CollectionUtils.createList;
import static com.opengamma.simm.utils.CollectionUtils.createMap;
import static java.util.stream.Collectors.toList;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import org.testng.annotations.Test;

import com.opengamma.simm.basics.StandardRiskFactor;
import com.opengamma.simm.basics.RiskFactor;

public class SimmUtilsTest {
  
  private static final double TOLERANCE_PERCENTILE = 1.0E-6;
  private static final double TOLERANCE_PL = 1.0E-6;

  private static final double VAR_LEVEL = 0.90; // 90% VaR
  private static final double[] VALUES =
    {10.0, 9,0, 5.0, 12.0, -5.0, 25.0, 0.0, -10.0, 0.5, -0.5,
    23.0, 24.0, 26.0, -2.0, -3.0, 45.0, -10.0, -11.0, -12.0, -13.0};
  
  @Test
  public void percentile() {

    List<Double> asList = DoubleStream.of(VALUES).mapToObj(d -> d).collect(toList());
    double percentileComputed = SimmUtils.percentile(asList, VAR_LEVEL);
    double[] valuesSorted = VALUES.clone();
    Arrays.sort(valuesSorted);
    int n = VALUES.length;
    int i = (int) Math.ceil((VAR_LEVEL - 0.5d / n) * n);
    double pi_1 = (-0.5 + i) / n;
    double pi = (0.5 + i) / n;
    double vi_1 = valuesSorted[i - 1];
    double vi = valuesSorted[i];
    assertTrue("SimmUtils: percentile", pi_1 <= VAR_LEVEL);
    assertTrue("SimmUtils: percentile", pi > VAR_LEVEL);
    int nbTests = 10;
    for (int looptest = 10; looptest <= nbTests; looptest++) {
      double level = pi_1 + (pi - pi_1) * ((double) looptest) / nbTests;
      assertTrue("SimmUtils: percentile - " + looptest, vi_1 <= SimmUtils.percentile(asList, level));
      assertTrue("SimmUtils: percentile - " + looptest, vi >= SimmUtils.percentile(asList, level));
    }
    double levelExpected = pi_1 + (percentileComputed - vi_1) / (vi - vi_1) * (pi - pi_1);
    assertEquals("SimmUtils: percentile", VAR_LEVEL, levelExpected, TOLERANCE_PERCENTILE);
  }

  private static final Map<RiskFactor, Double> DELTAS = createMap(
      StandardRiskFactor.of("RF1"), 100.0,
      StandardRiskFactor.of("RF2"), 200.0,
      StandardRiskFactor.of("RF3"), -200.0,
      StandardRiskFactor.of("RF4"), 0.0);

  private static final Map<RiskFactor, List<Double>> MVT = createMap(
      StandardRiskFactor.of("RF1"), createList(0.001, -0.005, -0.009, 0.006, 0.000, 0.000, 0.000, 0.002, 0.000, 0.000),
      StandardRiskFactor.of("RF2"), createList(0.002, 0.006, -0.010, 0.007, 0.006, -0.003, -0.003, -0.003, -0.010, -0.003),
      StandardRiskFactor.of("RF3"), createList(-0.003, 0.007, 0.001, 0.004, 0.007, 0.006, -0.010, 0.004, 0.001, 0.002),
      StandardRiskFactor.of("RF4"), createList(0.004, 0.008, 0.002, 0.006, 0.006, 0.007, 0.001, 0.006, 0.006, -0.003));
  
  @Test
  public void profits() {
    List<Double> profitsComputed = SimmUtils.profits(MVT, DELTAS);
    int numScenarios = MVT.values().iterator().next().size();
    assertEquals("SimmUtils: profits", profitsComputed.size(), numScenarios);

    IntStream.range(0, numScenarios)
        .forEach(i -> {
          double profit =
              DELTAS.entrySet()
                .stream()
                .collect(Collectors.summingDouble(
                    e -> MVT.get(e.getKey()).get(i) * e.getValue()));

          assertEquals("SimmUtils: percentile", profit, profitsComputed.get(i), TOLERANCE_PL);
        });
  }
  
}
