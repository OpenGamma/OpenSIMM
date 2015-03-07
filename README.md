[![OpenGamma](http://developers.opengamma.com/res/display/default/chrome/masthead_logo.png "OpenGamma")](http://www.opengamma.com)

# OpenSIMM by OpenGamma

This repository contains a standalone reference implementation of the ISDA-proposed Standard Initial Margin Model (SIMM) for non-cleared derivatives. The aim of OpenSIMM is to support standardization and adoption of the methodology which will be used industry-wide as the basis for the exchange of bilateral initial margin.

As the SIMM model has yet to be finalized, OpenGamma will continue to work with the industry on the standard as it evolves. We have implemented the initial HVaR-based approach in this release, but we are working with dealers on implementing the current version of the model, which uses a different methodology.

This new model will be available to those on the ISDA SIMM committee via a private GitHub repository in the next week. If you are interested in accessing this model when it becomes available, please contact us at simm@opengamma.com.

OpenSIMM is released as open source software under the 
[Apache v2.0 license](http://www.apache.org/licenses/LICENSE-2.0.html).

For more information please refer to our website, or contact us at simm@opengamma.com


## Releases

This repository contains version 1.0 as available in [Maven Central](http://search.maven.org/#artifactdetails%7Ccom.opengamma%7Copensimm%7C1.0%7Cjar).
OpenSIMM requires Java SE 8 or later.

    <dependency>
        <groupId>com.opengamma</groupId>
        <artifactId>opensimm</artifactId>
        <version>1.0</version>
    </dependency>

    
## Building OpenSIMM

Prerequisites:

- Java 8
- [Apache Maven](http://maven.apache.org/), version 3.0.4 or later

The source code can be cloned using [git](http://git-scm.com/) from GitHub:

    git clone https://github.com/OpenGamma/OpenSIMM.git

Simply run this command to compile and install the source code locally:

    mvn install

The project contains only one external dependency - TestNG which is required
for running the unit tests. Apart from this, the project is entirely stand-alone.


## Running OpenSIMM

The calculation can be run from the command line.

First run:

    mvn package

This will build a jar file: opensimm-1.0.jar

The jar file can then be executed:

    java -jar target/opensimm-1.0.jar src/test/resources/simm-sample/simm.properties

which should give the output similar to the following:

    ----------- ----------
    Asset Class Var
    ----------- ----------
    CO            564.3703
    IR            447.5351
    CR             33.3300
    EQ            740.7143
    ----------- ----------
    Total       1,785.9496
    ----------- ----------


# SIMM Calculator

The primary class in the system is the SimmCalculator
class. The following sections describe how to calculate
a VaR measure for asset classes using SIMM.

The calculator can either be used with raw data
provided programatically, or by loading csv files.


## Data Types

In order to perform the calculation a number of
different types of data need to be supplied:

- Risk Factors - risk factors describe categories
  that a portfolio can be split into. A risk factor
  has:
    - a unique name,
    - an asset class - one of IR (Interest Rate), CR (Credit), EQ (Equity) or CO (Commodity)
    - the risk type - one of SENSITIVITY or EXPOSURE
    - the shock type - either AB (Absolute) or RE (Relative). If relative then a shift can also be supplied.

- Risk Factor Base Levels - the base levels describe the
  starting value for each risk factor

- Risk Factor Shocks - the shocks are a set of perturbations
  to be applied to the base levels. These are then used in
  performing the VaR calculation.

- Base Currency - the currency that all output will be in

- FX Rates - the set of current FX rates for all currencies in the portfolio

- FX Rate Shocks - the set of shocks to be applied to the FX Rates

- Derivative Portfolio - the portfolio to be evaluated broken down
  into exposures against each Risk Factor.

- Initial Margin Portfolio - any initial margin broken down
  into exposures against each Risk Factor.

- Variation Margin Portfolio - any variation margin broken down
  into exposures against each Risk Factor.


## Executing Programmatically

To execute the calculation (for instance in a test):

    // Create risk factor names
    RiskFactor IBM = StandardRiskFactor.of("IBM");
    RiskFactor USD_IRSL3M_2Y = StandardRiskFactor.of("USD-IRSL3M-2Y");

    // create risk factor properties and put into map
    Map<RiskFactor, RiskFactorProperties> riskFactorProperties = new HashMap<>();
    riskFactorProperties.put(USD_IRSL3M_2Y, RiskFactorProperties.relativeShock(IR, SENSITIVITY, 0.04));
    riskFactorProperties.put(IBM, RiskFactorProperties.absoluteShock(CR, SENSITIVITY));

    // create risk factor base levels
    Map<RiskFactor, Double> baseLevels = new HashMap<>();
    baseLevels.put(USD_IRSL3M_2Y, 0.01);
    baseLevels.put(IBM, 0.0120);

    // create fx matrix for all currencies to be used
    FxMatrix fxMatrix = FxMatrix.builder()
        .addRate(EUR, USD, 1.40)
        .addRate(GBP, USD, 1.60)
        .build();

    // create risk factor shocks
    Map<RiskFactor, List<Double>> riskFactorShocks = new HashMap<>();
    riskFactorShocks.put(USD_IRSL3M_2Y,
        // Normally many, many more
        Arrays.asList(1.0025, 1.0025, 0.9975, 0.9975, 1.0000, 1.0000, 1.0000, 1.0025));
    riskFactorShocks.put(IBM, Arrays.asList(0.0001, -0.0005, -0.0050, 0.0002, -0.0006, -0.0051));

    // create FX shock
    Map<Pair<Currency, Currency>, List<Double>> fxShocks = new HashMap<>();
    // Shock length must match that for IR risk factors - USD_IRSL3M_2Y in this case
    fxShocks.put(Pair.of(EUR, USD),
        Arrays.asList(1.0000, 1.0010, 0.9975, 0.9950, 1.0002, 1.0100, 0.9950, 0.9970));
    fxShocks.put(Pair.of(GBP, USD),
        Arrays.asList(0.9985, 1.0020, 1.0000, 0.9950, 1.0003, 1.0100, 0.9940, 0.9960));

    // Now setup the calculator with all the data we have so far
    SimmCalculator calculator = SimmCalculator.builder()
        .varLevel(0.9)
        .baseCurrency(EUR)
        .riskFactors(riskFactorProperties)
        .riskFactorLevels(baseLevels)
        .fxMatrix(fxMatrix)
        .riskFactorShocks(riskFactorShocks)
        .fxShocks(fxShocks)
        .build();

    // Build the portfolio to be assessed
    List<PortfolioExposure> portfolio = Arrays.asList(
        PortfolioExposure.of(USD_IRSL3M_2Y, 250_000, USD),
        PortfolioExposure.of(IBM, 100_000, GBP),
        // Currency positions can be held too
        RawDelta.of(FxRiskFactor.of(GBP), 125_000, GBP));

    // It is also possible to provide current initial and variation
    // margining positions in the same format - see version of
    // calculator.varByAssetClass taking three arguments

    // Calculate the values for the portfolio
    Map<AssetClass, Double> result = calculator.varByAssetClass(portfolio);


## Executing from data files

It is possible to load the required data from csv files rather
than programmatically creating the data as above. A set of loaders
are provided to facilitate this.

    // getFile() method just returns a valid java.util.File,
    // its implementation is unimportant
    Map<RiskFactor, RiskFactorProperties> riskFactorProperties =
        RiskFactorDefinitionsLoader.of(getFile("risk-factor-definitions")).load();

    Map<RiskFactor, Double> riskFactorLevels =
        RiskFactorBaseLevelsLoader.of(getFile("risk-factor-base-levels")).load();

    FxMatrix fxMatrix = FxRateLoader.of(getFile("fx-rates")).load();

    Map<RiskFactor, List<Double>> riskFactorShocks =
        RiskFactorShocksLoader.of(getFile("risk-factor-shocks")).load();

    Map<Pair<Currency, Currency>, List<Double>> fxShocks =
        FxShocksLoader.of(getFile("fx-rate-shocks")).load();

    SimmCalculator calculator = SimmCalculator.builder()
        .varLevel(0.9)
        .baseCurrency(EUR)
        .riskFactors(riskFactorProperties)
        .riskFactorLevels(riskFactorLevels)
        .fxMatrix(fxMatrix)
        .riskFactorShocks(riskFactorShocks)
        .fxShocks(fxShocks)
        .build();

    Set<RiskFactor> riskFactors = riskFactors.keySet();
    List<RawDelta> derivativesPortfolio =
        PortfolioLoader.of(getFile("portfolio-derivatives"), riskFactors).load();
    List<RawDelta> initialMargin =
        PortfolioLoader.of(getFile("portfolio-initial-margin"), riskFactors).load();
    List<RawDelta> variationMargin =
        PortfolioLoader.of(getFile("portfolio-variation-margin"), riskFactors).load();

    Map<AssetClass, Double> var =
        calculator.varByAssetClass(derivativesPortfolio, initialMargin, variationMargin);


## File Formats

The file formats are straightforward - just simple CSV files. There are examples
in test/resources/simm-sample.


### Risk Factor Definitions

Contains the definitions of the fundamentals of the risk factors.

    RiskFactorName, AssetClass, RiskType,    ShockType, Shift
    USD-IRSL3M-2Y,  IR,         SENSITIVITY, RE,        0.04
    IBM,            CR,         SENSITIVITY, AB
    SP500,          EQ,         EXPOSURE,    RE

- RiskFactorName - the risk factor name, can be any String but must be unique in the file
- AssetClass - the asset class the risk factor belongs to - one of IR, CR, EQ or CO
- RiskType - the risk type of the risk factor - one of SENSITIVITY or EXPOSURE
- ShockType - the type of shocks - one of RELATIVE or ABSOLUTE
- Shift - if the ShockType was RELATIVE, a shift can be supplied here

### Risk Factor Base Levels

Contains the initial levels for the risk factors.

    RiskFactorName, BaseLevel
    USD-IRSL3M-2Y,  0.01
    IBM,            0.012
    SP500,          1000

- RiskFactorName - the risk factor name, must be defined in the Risk Factor Definitions
- BaseLevel - the initial level for the risk factor

### Risk Factor Shocks

Contains the shocks to be applied to the risk factor levels.

    RiskFactorName, Shocks
    USD-IRSL3M-2Y, 1.0025, 1.0025, 0.9975, 0.9975, 1, 1, 1.0025, 0.9975, 1, 1, 1, 1, 1, 1, 1.0025, 0.9975, 1, 1, 1, 1, 1.0025, 0.9975, 1, 1, 1
    IBM, 0.0001, -0.0005, -0.005, 0.0002, -0.0006, -0.0051, 0.0003, -0.0007, -0.0052, 0, 0.0004, -0.0001, 0.0011, 0.0008, 0.0012
    SP500, 1.011, 0.995, 0.997, 1.001, 1.0002, 1.01, 0.9955, 0.9975, 1.0012, 1.0003, 1.0101, 0.9951

- RiskFactorName - the risk factor name, must be defined in the Risk Factor Definitions
- Shocks - list of shocks to be applied to the base levels. All shocks for a particular
  asset class must be the same length. Additionally, the shocks for IR asset class must
  be the same length as the FX shocks in the FX Shocks file.

### FX Rates

Contains the base FX rates.

    CurrencyPair, Rate
    EUR/USD,      1.4
    GBP/USD,      1.6

- CurrencyPair - the currency pair the rate is for, format is CCY1/CCY2
- Rate - the base FX rate for the currency pair - the EUR/USD line in the
  sample above is read as "1 EUR = 1.4 USD"

### FX Shocks

Contains the shocks to be applied to the FX rates.

    CurrencyPair, Shocks
    EUR/USD, 1, 1.001, 0.9975, 0.995, 1.0002, 1.01, 0.995, 0.997, 1.001, 1.0002, 1.01, 0.995, 0.997, 1.001, 1.0002, 1.01, 0.995, 0.997, 1.001, 1.0002, 1.01, 0.995, 0.997, 1.001, 1.0002
    GBP/USD, 0.9985, 1.002, 1, 0.995, 1.0003, 1.01, 0.994, 0.996, 1.001, 1.0002, 1.01, 1.005, 0.9999, 0.9999, 1.0011, 1.005, 0.996, 0.998, 1.0025, 1.01, 0.995, 0.997, 1.001, 1.0002, 1.0001

- CurrencyPair - the currency pair the shocks are for, must be defined in the FX Rates file.
- Shocks - list of shocks to be applied to the base FX rates. All shocks must be the same length,
  and must be the same length as the shocks for the IR asset class in the Risk Factor Shocks file.

### Portfolio File

Contains the exposures of a portfolio to the various risk factors. The same file format
is used for the portfolio, plus any offsetting initial or variation margin files.

    RiskFactorName, Amount,   Currency
    EUR-OIS-2Y,     100000,   EUR
    EUR-OIS-5Y,     -20000,   EUR
    USD-IRSL3M-2Y,  20000,    EUR
    IBM,            30300,    EUR
    SP500,          100000,   USD

- RiskFactorName - the risk factor name, must be defined in the Risk Factor Definitions
- Amount - the amount of the exposure to the risk factor
- Currency - the currency for the Amount. Must be defined in the FX Rates file.

### Properties File

It is possible to define a properties file holding the details of the
data files, which can simplify the loading process.

    base-currency=EUR
    var-level=0.9
    risk-factor-definitions=src/test/resources/simm-sample/risk-factor-definitions.csv
    risk-factor-base-levels=src/test/resources/simm-sample/risk-factor-base-levels.csv
    risk-factor-shocks=src/test/resources/simm-sample/risk-factor-shocks.csv
    fx-rates=src/test/resources/simm-sample/fx-rates.csv
    fx-rate-shocks=src/test/resources/simm-sample/fx-rate-shocks.csv
    portfolio-derivatives=src/test/resources/simm-sample/portfolio-derivatives.csv
    portfolio-initial-margin=src/test/resources/simm-sample/portfolio-initial-margin.csv
    portfolio-variation-margin=src/test/resources/simm-sample/portfolio-variation-margin.csv

- base-currency - the base currency for the calculation. All results will be in the base currency.
- var-level - Optional field allowing the VaR confidence level to be set. Not generally required
  as standard default value of 99% is set in the code. However, for small data sets a reduced
  value may be required.
- risk-factor-definitions - path to the risk factor definitions file
- risk-factor-base-levels - path to the risk factor base levels file
- risk-factor-shocks - path to the risk factor shock file
- fx-rates - path to the FX rates file
- fx-rate-shocks - path to the FX rate shocks file
- portfolio-derivatives - path to the portfolio file
- portfolio-initial-margin - Optional field allowing the path to a portfolio file for any initial margin to be set
- portfolio-variation-margin - Optional field allowing the path to a portfolio file for any variation margin to be set
