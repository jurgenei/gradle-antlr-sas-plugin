# Gradle ANTLR SAS Plugin (Proof of Concept)

`gradle-antlr-sas-plugin` provides two chained Gradle tasks for SAS parsing experiments:

1. `SasMacroGradleTask` (`sasMacro`) expands a focused subset of SAS macros.
2. `XmlAstSasGradleTask` (`sasXmlAst`) converts expanded SAS into XML AST using ANTLR.

The repository also provides an independent CASL module:

- `XmlAstCaslGradleTask` (`caslXmlAst`) converts CASL sources to XML AST.
- `CaslSemanticExtractGradleTask` (`caslSemantic`) converts CASL XML AST into normalized semantic JSON.
- `caslPipeline` runs CASL conversion pipeline wrapper.

The repository also provides an independent DS2 module:

- `XmlAstDs2GradleTask` (`ds2XmlAst`) converts DS2 sources to XML AST.
- `Ds2SemanticExtractGradleTask` (`ds2Semantic`) extracts DS2 semantic JSON summaries.
- `ds2Pipeline` runs DS2 XML AST + semantic extraction pipeline.

Pipeline:

```text
SasProgramFile -> sasMacro -> sasXmlAst -> XML AST
```

CASL pipeline:

```text
CaslProgramFile -> caslXmlAst -> caslSemantic -> XML AST + semantic JSON
```

DS2 pipeline:

```text
Ds2ProgramFile -> ds2XmlAst -> ds2Semantic -> XML AST + semantic JSON
```

## Current Scope

Implemented in this proof of concept:

- Plugin foundation and task registration (`SasGrammarPlugin`)
- Macro processor architecture (`SasMacroProcessor` + `SasMacroGradleTask`)
- Minimal DATA step grammar support
- Minimal PROC SQL grammar support
- Independent CASL module and grammar slice
- Independent FEDSQL grammar module for `fedSql.execDirect` payload validation
- Independent DS2 module and grammar slice
- XML AST generation via `XmlAstGradleTask` base task
- Automated regression framework using fixture-driven Gradle functional tests

Not implemented yet:

- Full SAS language coverage
- Full macro language (parameters, macro functions, quoting functions)
- Full CASL action language and FEDSQL modules

## Plugin IDs

- Traditional SAS: `name.jurgenei.gradle.antlr.sas`
- CASL: `name.jurgenei.gradle.antlr.casl`
- DS2: `name.jurgenei.gradle.antlr.ds2`

## Install (Local Development)

```bash
./gradlew -p gradle-antlr-sas-plugin clean test
```

Plugin id:

```groovy
plugins {
	id 'name.jurgenei.gradle.antlr.sas'
}
```

## Tasks

- `sasMacro`: expands `%let` variables and `&var` / `&var.` references
- `sasXmlAst`: parses expanded files with `name.jurgenei.parsers.SasLexer` + `name.jurgenei.parsers.SasParser`
- `sasPipeline`: convenience wrapper running both stages
- `caslXmlAst`: converts CASL files to XML AST
- `caslSemantic`: extracts normalized JSON IR from CASL XML AST output
- `caslPipeline`: convenience wrapper running CASL XML AST + semantic extraction stages
- `ds2XmlAst`: converts DS2 files to XML AST
- `ds2Semantic`: extracts DS2 semantic JSON summaries
- `ds2Pipeline`: convenience wrapper running DS2 XML AST + semantic extraction stages

Default conventions:

- `sasMacro.sourceDirectory = src/main/sas`
- `sasMacro.destinationDirectory = build/sas/macro`
- `sasXmlAst.sourceDirectory = build/sas/macro` (wired from `sasMacro` output)
- `sasXmlAst.destinationDirectory = build/sas/xmlast`
- `sasXmlAst.startRule = program`
- `caslXmlAst.sourceDirectory = src/main/casl`
- `caslXmlAst.destinationDirectory = build/casl/xmlast`
- `caslXmlAst.startRule = program`
- `caslSemantic.sourceDirectory = build/casl/xmlast`
- `caslSemantic.destinationDirectory = build/casl/semantic`
- `ds2XmlAst.sourceDirectory = src/main/ds2`
- `ds2XmlAst.destinationDirectory = build/ds2/xmlast`
- `ds2XmlAst.startRule = program`
- `ds2Semantic.sourceDirectory = src/main/ds2`
- `ds2Semantic.destinationDirectory = build/ds2/semantic`

## Grammar Coverage (POC)

DATA step:

- `data <name>; ... run;`
- dataset options in header (`keep=...`, `drop=...`)
- `set <name>;`
- assignment statements with arithmetic and grouping (`x = (a * b) - c;`)
- `format` statements (POC slice)
- `output;`

PROC SQL:

- `proc sql; ... quit;`
- `create table <name> as select ... from ... ;`
- `select <cols|*> from <name> [where <expr>]`
- `left|right|inner join ... on ...` (POC slice)
- boolean predicates with `and` / `or`

CASL:

- assignment (`x = 1;`)
- action calls (`simple.summary / table={name='cars'};`)
- action-set slices: `table.loadTable`, `fedSql.execDirect`
- arrays and objects (`[1,2]`, `{name='cars'}`)
- conditional statements (`if x > 1 then run;`)

DS2:

- `proc ds2; ... run; quit;`
- `data <name>; ... enddata;`
- `package <name>; ... endpackage;`
- `thread <name>; ... endthread;`
- `method <name>(); ... endmethod;`
- typed method params (`method m(varchar name);`)
- `set from <source>;`
- declaration slices: `dcl`, `declare`
- assignment and basic method-call statements

## Macro Expansion Coverage (POC)

Supported:

- `%let name = value;`
- `%macro name; ... %mend;`
- `%name;` macro invocation
- references: `&name` and `&name.`
- predefined Gradle-supplied macros (`sasMacro.predefinedMacros`)
- unresolved macro reporting (`sasMacro.failOnUndefinedMacro`)

## Shared Semantic Interfaces

Both SAS and CASL modules expose shared semantic contracts for future normalization:

- `name.jurgenei.gradle.antlr.semantic.LanguageModule`
- `name.jurgenei.gradle.antlr.semantic.SemanticProgram`
- module descriptors: `SasLanguageModule`, `CaslLanguageModule`

`caslSemantic` uses AST-driven CASL parsing (not regex scanning) and emits FEDSQL metrics:

- `fedSqlQueryCount`
- `fedSqlParsedCount`
- `fedSqlFailedCount`
- `fedSqlErrors`

## Usage Example

```groovy
plugins {
	id 'java'
	id 'name.jurgenei.gradle.antlr.sas'
}

tasks.named('sasMacro', name.jurgenei.gradle.antlr.SasMacroGradleTask) {
	predefinedMacros.put('ENV', 'dev')
}

tasks.named('sasXmlAst', name.jurgenei.gradle.antlr.XmlAstSasGradleTask) {
	continueOnError.set(false)
	failOnError.set(true)
}
```

Run pipeline:

```bash
./gradlew sasPipeline
```

## TDD and Regression Strategy

Test layers:

- Unit tests for macro processor behavior
- Plugin unit tests for registration, defaults, and task chaining
- Functional tests for end-to-end pipeline execution
- Fixture-driven regression harness in `src/test/resources/regression/*`
- CASL fixture-driven regression harness in `src/test/resources/regression-casl/*`
- Includes: `case-basic`, `case-actions`, `case-actions-variants`, `case-fedsql-queries`, `case-mixed`
- DS2 fixture-driven regression harness in `src/test/resources/regression-ds2/*`
- Includes: `case-basic`, `case-method-run`, `case-declarations`

DS2 semantic JSON now includes:

- `methods`, `methodCount`
- `declarations`, `declarationCount`
- `callEdges`, `callEdgeCount`
- `procDs2BlockCount`
- `dataBlocks`, `packageBlocks`, `threadBlocks`
- `setFromSources`
- Includes baseline + extended grammar cases (`case-basic`, `case-extended`)

Run full verification:

```bash
./gradlew -p gradle-antlr-sas-plugin clean test jacocoTestReport
```

## Architecture Notes

This module targets incremental delivery:

1. Stable two-stage pipeline (`sasMacro` -> `sasXmlAst`)
2. Expand grammar and macro features in test-backed slices
3. Expand independent CASL module, reusing shared semantic interfaces

CASL direction is intentionally separate from traditional SAS because syntax and execution model differ.
