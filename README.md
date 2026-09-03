# Gradle ANTLR SAS Plugin (Proof of Concept)

![Conformance](https://img.shields.io/badge/Conformance-Check--All%20Passing-brightgreen)

[![Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/name.jurgenei.gradle.antlr.sas?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/name.jurgenei.gradle.antlr.sas)
[![Build and Test](https://github.com/jurgenei/gradle-antlr-sas-plugin/actions/workflows/gradle-build.yml/badge.svg)](https://github.com/jurgenei/gradle-antlr-sas-plugin/actions/workflows/gradle-build.yml)
[![Coverage CI](https://github.com/jurgenei/gradle-antlr-sas-plugin/actions/workflows/coverage.yml/badge.svg)](https://github.com/jurgenei/gradle-antlr-sas-plugin/actions/workflows/coverage.yml)
[![Coverage](https://codecov.io/gh/jurgenei/gradle-antlr-sas-plugin/branch/main/graph/badge.svg)](https://codecov.io/gh/jurgenei/gradle-antlr-sas-plugin)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/java-21+-green.svg)](https://www.oracle.com/java/)
[![Gradle](https://img.shields.io/badge/gradle-8+-blue.svg)](https://gradle.org/)

`gradle-antlr-sas-plugin` exposes one plugin id:

- `name.jurgenei.gradle.antlr.sas`

One plugin registers three pipelines:

- SAS: `sasMacro` -> `sasXmlAst` -> `sasPipeline`
- CASL: `caslXmlAst` -> `caslSemantic` -> `caslPipeline`
- DS2: `ds2XmlAst` -> `ds2Semantic` -> `ds2Pipeline`

## Install (Local Development)

```bash
./gradlew -p gradle-antlr-sas-plugin clean test
```

## Plugin Usage

```groovy
plugins {
    id 'java'
    id 'name.jurgenei.gradle.antlr.sas'
}
```

## Tasks

- `sasMacro`: expands `%let` variables and `&var` / `&var.` references
- `sasXmlAst`: parses expanded SAS and writes XML AST
- `sasPipeline`: runs SAS macro + XML AST stages
- `caslXmlAst`: parses CASL sources and writes XML AST
- `caslSemantic`: extracts normalized semantic JSON from CASL pipeline
- `caslPipeline`: runs CASL XML AST + semantic stages
- `ds2XmlAst`: parses DS2 sources and writes XML AST
- `ds2Semantic`: extracts DS2 semantic JSON summaries
- `ds2Pipeline`: runs DS2 XML AST + semantic stages

## Default Conventions

- `sasMacro.sourceDirectory = src/main/sas`
- `sasMacro.destinationDirectory = build/sas/macro`
- `sasXmlAst.sourceDirectory = build/sas/macro`
- `sasXmlAst.destinationDirectory = build/sas/xmlast`
- `caslXmlAst.sourceDirectory = src/main/casl`
- `caslXmlAst.destinationDirectory = build/casl/xmlast`
- `caslSemantic.sourceDirectory = build/casl/xmlast`
- `caslSemantic.destinationDirectory = build/casl/semantic`
- `ds2XmlAst.sourceDirectory = src/main/ds2`
- `ds2XmlAst.destinationDirectory = build/ds2/xmlast`
- `ds2Semantic.sourceDirectory = src/main/ds2`
- `ds2Semantic.destinationDirectory = build/ds2/semantic`

## Quick Run

```bash
./gradlew -p gradle-antlr-sas-plugin sasPipeline
./gradlew -p gradle-antlr-sas-plugin caslPipeline
./gradlew -p gradle-antlr-sas-plugin ds2Pipeline
```

## Samples

Runnable samples live in `samples/`:

- `samples/sas`
- `samples/casl`
- `samples/ds2`

Each sample uses composite build in `settings.gradle`:

```groovy
pluginManagement {
    includeBuild('../..')
}
```

Run samples from `gradle-antlr-sas-plugin` directory:

```bash
./gradlew -p samples/sas sasPipeline
./gradlew -p samples/casl caslPipeline
./gradlew -p samples/ds2 ds2Pipeline
```

See `samples/README.md` for output locations.

## Scope (POC)

Implemented:

- Plugin and task registration in `SasGrammarPlugin`
- SAS macro expansion subset (`%let`, `%macro`, `%mend`, `%name`, `&name`)
- SAS XML AST generation via ANTLR
- CASL XML AST + semantic extraction
- DS2 XML AST + semantic extraction
- Functional and regression test harnesses

Not implemented:

- Full SAS language coverage
- Full macro language semantics
- Full CASL action language
- Full DS2 language coverage

## Verification

```bash
./gradlew -p gradle-antlr-sas-plugin clean test jacocoTestReport
```
