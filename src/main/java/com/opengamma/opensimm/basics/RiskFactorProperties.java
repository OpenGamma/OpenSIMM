/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.opensimm.basics;

/**
 * Describes the properties of a risk factor.
 */
public class RiskFactorProperties {

  /**
   * The asset class.
   */
  private final AssetClass assetClass;

  /**
   * The risk type.
   */
  private final RiskType riskType;

  /**
   * The shock. May be absolute or relative.
   */
  private final ShockType shockType;

  /**
   * Create risk factor properties containing the asset class and risk type with
   * a relative shock, including a shift.
   *
   * @param assetClass  the asset class for the risk factor
   * @param riskType  the risk type for the risk factor
   * @param shift  the shift to be used when applying a relative shock
   * @return a new {@code RiskFactorProperties} instance
   */
  public static RiskFactorProperties relativeShock(
      AssetClass assetClass,
      RiskType riskType,
      double shift) {
    return new RiskFactorProperties(assetClass, riskType, RelativeShockType.of(shift));
  }

  /**
   * Create risk factor properties containing the asset class and risk type with
   * a relative shock with no shift.
   *
   * @param assetClass  the asset class for the risk factor
   * @param riskType  the risk type for the risk factor
   * @return a new {@code RiskFactorProperties} instance
   */
  public static RiskFactorProperties relativeShock(
      AssetClass assetClass,
      RiskType riskType) {
    return new RiskFactorProperties(assetClass, riskType, RelativeShockType.of());
  }

  /**
   * Create risk factor properties containing the asset class and risk type with
   * an absolute shock.
   *
   * @param assetClass  the asset class for the risk factor
   * @param riskType  the risk type for the risk factor
   * @return a new {@code RiskFactorProperties} instance
   */
  public static RiskFactorProperties absoluteShock(
      AssetClass assetClass,
      RiskType riskType) {
    return new RiskFactorProperties(assetClass, riskType, AbsoluteShockType.of());
  }

  /**
   * Return the asset class for the risk factor.
   *
   * @return the asset class
   */
  public AssetClass getAssetClass() {
    return assetClass;
  }

  /**
   * Return the risk type for the risk factor.
   *
   * @return the risk type
   */
  public RiskType getRiskType() {
    return riskType;
  }

  /**
   * Return the shock type for the risk factor.
   *
   * @return the shock type
   */
  public ShockType getShockType() {
    return shockType;
  }

  // Private constructor
  private RiskFactorProperties(AssetClass assetClass, RiskType riskType, ShockType shockType) {
    this.assetClass = assetClass;
    this.riskType = riskType;
    this.shockType = shockType;
  }

}
