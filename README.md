# PowSyBl Single Line Diagram

[![Build Status](https://api.travis-ci.com/powsybl/powsybl-single-line-diagram.svg?branch=master)](https://travis-ci.com/powsybl/powsybl-single-line-diagram)
[![Build status](https://ci.appveyor.com/api/projects/status/at7jfgmxghc45esj/branch/master?svg=true)](https://ci.appveyor.com/project/powsybl/powsybl-single-line-diagram/branch/master)
[![Coverage Status](https://sonarcloud.io/api/project_badges/measure?project=com.powsybl%3Apowsybl-single-line-diagram&metric=coverage)](https://sonarcloud.io/component_measures?id=com.powsybl%3Apowsybl-single-line-diagram&metric=coverage)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=com.powsybl%3Apowsybl-single-line-diagram&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.powsybl%3Apowsybl-single-line-diagram)
[![MPL-2.0 License](https://img.shields.io/badge/license-MPL_2.0-blue.svg)](https://www.mozilla.org/en-US/MPL/2.0/)
[![Join the community on Spectrum](https://withspectrum.github.io/badge/badge.svg)](https://spectrum.chat/powsybl)

PowSyBl (**Pow**er **Sy**stem **Bl**ocks) is an open source framework written in Java, that makes it easy to write complex
software for power systems’ simulations and analysis. Its modular approach allows developers to extend or customize its
features.

PowSyBl is part of the LF Energy Foundation, a project of The Linux Foundation that supports open source innovation projects
within the energy and electricity sectors.

<p align="center">
<img src="https://raw.githubusercontent.com/powsybl/powsybl-gse/master/gse-spi/src/main/resources/images/logo_lfe_powsybl.svg?sanitize=true" alt="PowSyBl Logo" width="50%"/>
</p>

Read more at https://www.powsybl.org !

This project and everyone participating in it is governed by the [PowSyBl Code of Conduct](https://github.com/powsybl/.github/blob/master/CODE_OF_CONDUCT.md).
By participating, you are expected to uphold this code. Please report unacceptable behavior to [powsybl-tsc@lists.lfenergy.org](mailto:powsybl-tsc@lists.lfenergy.org).

## PowSyBl vs PowSyBl Single Line Diagram

PowSyBl Single Line Diagram is a component build on top of `Network` model available in PowSyBl Core repository responsible 
for generating [single line diagram](https://en.wikipedia.org/wiki/One-line_diagram).
 - Node/Breaker and bus/breaker topology.
 - [SVG](https://fr.wikipedia.org/wiki/Scalable_Vector_Graphics) diagram to be used in various front-end technologies.
 - Voltage level and substation diagram (and zone diagram soon).
 - Highly customizable rendering using equipment component libraries, CSS and configurable labels (position and content).
 - Fully automatic layout
 - Semi-automatic layout using relative positions for busbar sections and feeders
 - CGMES DL layout support

![Diagram demo](.github/diagram-demo.svg)

## Getting started

To generate an SVG single line diagram from a voltage level, we first need to add a few Maven dependency respectively 
for `Network` model, `Network` test cases and simple logging capabilities:

```xml
<dependency>
    <groupId>com.powsybl</groupId>
    <artifactId>powsybl-iidm-impl</artifactId>
    <version>3.0.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>com.powsybl</groupId>
    <artifactId>powsybl-iidm-test</artifactId>
    <version>3.0.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-simple</artifactId>
    <version>1.7.28</version>
</dependency>
```

We can now load a node/breaker test `Network` and get voltage level "c":
```java
Network network = FictitiousSwitchFactory.create();
VoltageLevel c = network.getVoltageLevel("C");
```

After adding the single line diagram core module dependency:
```xml
<dependency>
    <groupId>com.powsybl</groupId>
    <artifactId>powsybl-single-line-diagram-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

We can generate a SVG for voltage level "C":
```java
// "Convergence" style component library
ComponentLibrary componentLibrary = new ResourcesComponentLibrary("/ConvergenceLibrary");

// fully automatic layout
VoltageLevelLayoutFactory voltageLevelLayoutFactory = new PositionVoltageLevelLayoutFactory(new PositionByClustering());

//  create diagram
VoltageLevelDiagram voltageLevelDiagram = VoltageLevelDiagram.build(c, voltageLevelLayoutFactory, false, false);

// generate SVG
voltageLevelDiagram.writeSvg("",
                             new DefaultSVGWriter(componentLibrary, new LayoutParameters()),
                             new DefaultDiagramInitialValueProvider(network),
                             new NominalVoltageDiagramStyleProvider(),
                             new DefaultNodeLabelConfiguration(componentLibrary),
                             Paths.get("/tmp/c.svg"),
                             false);
```
