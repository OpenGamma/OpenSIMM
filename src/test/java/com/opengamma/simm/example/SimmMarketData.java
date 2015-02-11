package com.opengamma.simm.example;

import static com.opengamma.simm.basics.AssetClass.CO;
import static com.opengamma.simm.basics.AssetClass.CR;
import static com.opengamma.simm.basics.AssetClass.EQ;
import static com.opengamma.simm.basics.AssetClass.IR;
import static com.opengamma.simm.basics.RiskFactorProperties.RiskType.EXPOSURE;
import static com.opengamma.simm.basics.RiskFactorProperties.RiskType.SENSITIVITY;
import static com.opengamma.simm.utils.CollectionUtils.createList;
import static com.opengamma.simm.utils.CollectionUtils.createMap;

import java.util.Currency;
import java.util.List;
import java.util.Map;

import com.opengamma.simm.basics.FxRiskFactor;
import com.opengamma.simm.basics.StandardRiskFactor;
import com.opengamma.simm.basics.RiskFactor;
import com.opengamma.simm.basics.RiskFactorProperties;
import com.opengamma.simm.utils.Pair;

public class SimmMarketData {

  public static Currency USD = Currency.getInstance("USD");
  public static Currency EUR = Currency.getInstance("EUR");
  public static Currency GBP = Currency.getInstance("GBP");

  public static final FxRiskFactor EUR_RF = FxRiskFactor.of(EUR);
  public static final FxRiskFactor USD_RF = FxRiskFactor.of(USD);
  public static final FxRiskFactor GBP_RF = FxRiskFactor.of(GBP);

  public static final StandardRiskFactor EUR_OIS_2Y = StandardRiskFactor.of("EUR-OIS-2Y");
  public static final StandardRiskFactor EUR_OIS_5Y = StandardRiskFactor.of("EUR-OIS-5Y");
  public static final StandardRiskFactor USD_IRSL3M_2Y = StandardRiskFactor.of("USD-IRSL3M-2Y");
  public static final StandardRiskFactor IBM = StandardRiskFactor.of("IBM");
  public static final StandardRiskFactor SP500 = StandardRiskFactor.of("SP500");
  public static final StandardRiskFactor XAU = StandardRiskFactor.of("XAU");

  public static final Map<RiskFactor, RiskFactorProperties> RISK_FACTOR_NON_FX = createMap(
      EUR_OIS_2Y, RiskFactorProperties.absoluteShock(IR, SENSITIVITY),
      EUR_OIS_5Y, RiskFactorProperties.absoluteShock(IR, SENSITIVITY),
      USD_IRSL3M_2Y, RiskFactorProperties.relativeShock(IR, SENSITIVITY, 0.04),
      IBM, RiskFactorProperties.absoluteShock(CR, SENSITIVITY),
      SP500, RiskFactorProperties.relativeShock(EQ, EXPOSURE),
      XAU, RiskFactorProperties.relativeShock(CO, EXPOSURE));

  public static final  Map<Pair<Currency, Currency>, Double> FX_RATES = createMap(
      Pair.of(EUR, USD), 1.40,
      Pair.of(GBP, USD), 1.60);

  /**
   * The initial market levels for non-FX items. The shocks are then applied
   * to the initial value in sequence.
   */
  public static final Map<RiskFactor, Double> INITIAL_MARKET_LEVELS = createMap(
      EUR_OIS_2Y, -0.0005,
      EUR_OIS_5Y, 0.0025,
      USD_IRSL3M_2Y, 0.01,
      IBM, 0.0120,
      SP500, 1000.0,
      XAU, 1200.0);

  private static final  List<Double> SHOCKS_EUR_OIS_2Y = createList(
      0.0001,
      -0.0005,
      -0.0050,
      0.0000,
      0.0002,
      0.0001,
      -0.0005,
      -0.0050,
      0.0001,
      -0.0005,
      -0.0050,
      0.0000,
      0.0000,
      0.0002,
      0.0001,
      -0.0005,
      -0.0050,
      0.0000,
      0.0002,
      0.0002,
      0.0001,
      -0.0005,
      -0.0050,
      0.0000,
      0.0002);

  private static final  List<Double> SHOCKS_USD_IRSL3M_2Y = createList(
      1.0025,
      1.0025,
      0.9975,
      0.9975,
      1.0000,
      1.0000,
      1.0000,
      1.0025,
      0.9975,
      1.0000,
      1.0000,
      1.0000,
      1.0000,
      1.0000,
      1.0025,
      0.9975,
      1.0000,
      1.0000,
      1.0000,
      1.0000,
      1.0025,
      0.9975,
      1.0000,
      1.0000,
      1.0000);

  private static final  List<Double> SHOCKS_EUR_USD = createList(
      1.0000,
      1.0010,
      0.9975,
      0.9950,
      1.0002,
      1.0100,
      0.9950,
      0.9970,
      1.0010,
      1.0002,
      1.0100,
      0.9950,
      0.9970,
      1.0010,
      1.0002,
      1.0100,
      0.9950,
      0.9970,
      1.0010,
      1.0002,
      1.0100,
      0.9950,
      0.9970,
      1.0010,
      1.0002);

  private static final  List<Double> SHOCKS_GBP_USD = createList(
      0.9985,
      1.0020,
      1.0000,
      0.9950,
      1.0003,
      1.0100,
      0.9940,
      0.9960,
      1.0010,
      1.0002,
      1.0100,
      1.0050,
      0.9999,
      0.9999,
      1.0011,
      1.0050,
      0.9960,
      0.9980,
      1.0025,
      1.0100,
      0.9950,
      0.9970,
      1.0010,
      1.0002,
      1.0001);

  private static final  List<Double> SHOCKS_IBM = createList(
      0.0001,
      -0.0005,
      -0.0050,
      0.0002,
      -0.0006,
      -0.0051,
      0.0003,
      -0.0007,
      -0.0052,
      0.0000,
      0.0004,
      -0.0001,
      0.0011,
      0.0008,
      0.0012);

  private static final  List<Double> SHOCKS_SP500 = createList(
      1.0110,
      0.9950,
      0.9970,
      1.0010,
      1.0002,
      1.0100,
      0.9955,
      0.9975,
      1.0012,
      1.0003,
      1.0101,
      0.9951);

  private static final  List<Double> SHOCKS_XAU = createList(
      1.0100,
      0.9950,
      0.9970,
      1.0010,
      0.9955,
      1.0002,
      1.0110,
      0.9940,
      0.9975,
      1.0030,
      0.9900,
      1.0004,
      1.0120,
      0.9945);

  public static final Map<RiskFactor, List<Double>> RF_SHOCKS = createMap(
      EUR_OIS_2Y, SHOCKS_EUR_OIS_2Y,
      USD_IRSL3M_2Y, SHOCKS_USD_IRSL3M_2Y,
      IBM, SHOCKS_IBM,
      SP500, SHOCKS_SP500,
      XAU, SHOCKS_XAU);

  public static final Map<Pair<Currency, Currency>, List<Double>> FX_SHOCKS = createMap(
      Pair.of(EUR, USD), SHOCKS_EUR_USD,
      Pair.of(GBP, USD), SHOCKS_GBP_USD);


}
