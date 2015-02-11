/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.simm.basics;

/**
 * Describes the properties of a risk factor.
 */
public class RiskFactorProperties {

  public enum RiskType {
    SENSITIVITY, EXPOSURE;
  }

  /** The asset class. Supported are "IR", "FX", "CR", "EQ", and "CO". */
  private final AssetClass assetClass;

  /** The risk type. Supported types are "SE": sensitivity and "EX": exposure. */
  private final RiskType riskType;

  /** The shock. May be absolute or relative. */
  private final ShockType shockType;

  public static RiskFactorProperties relativeShock(AssetClass assetClass, RiskType riskType, double shift) {
    return new RiskFactorProperties(assetClass, riskType, RelativeShockType.of(shift));
  }

  public static RiskFactorProperties relativeShock(AssetClass assetClass, RiskType riskType) {
    return new RiskFactorProperties(assetClass, riskType, RelativeShockType.of());
  }

  public static RiskFactorProperties absoluteShock(AssetClass assetClass, RiskType riskType) {
    return new RiskFactorProperties(assetClass, riskType, AbsoluteShockType.of());
  }

  /**
   * Constructor.
   * @param assetClass The asset class. Supported are "IR", "FX", "CR", "EQ", and "CO".
   * @param riskType The risk type. Supported types are "SE": sensitivity and "EX": exposure.
   * @param shockType The shock .
   */
  private RiskFactorProperties(AssetClass assetClass, RiskType riskType, ShockType shockType) {
    this.assetClass = assetClass;
    this.riskType = riskType;
    this.shockType = shockType;
  }

  public AssetClass getAssetClass() {
    return assetClass;
  }

  public RiskType getRiskType() {
    return riskType;
  }

  public ShockType getShockType() {
    return shockType;
  }

}
