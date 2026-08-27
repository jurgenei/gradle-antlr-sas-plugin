# Gradle ANTLR SAS Plugin (Proof of Concept)

`gradle-antlr-sas-plugin` provides two chained Gradle tasks for SAS parsing experiments:

1. `SasMacroGradleTask` (`sasMacro`) expands a focused subset of SAS macros.
2. `XmlAstSasGradleTask` (`sasXmlAst`) converts expanded SAS into XML AST using ANTLR.

Pipeline:

```text
SasProgramFile -> sasMacro -> sasXmlAst -> XML AST
```

## Current Scope

Implemented in this proof of concept:

- Plugin foundation and task registration (`SasGrammarPlugin`)
- Macro processor architecture (`SasMacroProcessor` + `SasMacroGradleTask`)
- Minimal DATA step grammar support
- Minimal PROC SQL grammar support
- XML AST generation via `XmlAstGradleTask` base task
- Automated regression framework using fixture-driven Gradle functional tests

Not implemented yet:

- Full SAS language coverage
- Full macro language (`%macro/%mend`, macro functions, quoting functions)
- CASL/FEDSQL modules

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

Default conventions:

- `sasMacro.sourceDirectory = src/main/sas`
- `sasMacro.destinationDirectory = build/sas/macro`
- `sasXmlAst.sourceDirectory = build/sas/macro` (wired from `sasMacro` output)
- `sasXmlAst.destinationDirectory = build/sas/xmlast`
- `sasXmlAst.startRule = program`

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

## Macro Expansion Coverage (POC)

Supported:

- `%let name = value;`
- `%macro name; ... %mend;`
- `%name;` macro invocation
- references: `&name` and `&name.`
- predefined Gradle-supplied macros (`sasMacro.predefinedMacros`)
- unresolved macro reporting (`sasMacro.failOnUndefinedMacro`)

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
- Includes baseline + extended grammar cases (`case-basic`, `case-extended`)

Run full verification:

```bash
./gradlew -p gradle-antlr-sas-plugin clean test jacocoTestReport
```

## Architecture Notes

This module targets incremental delivery:

1. Stable two-stage pipeline (`sasMacro` -> `sasXmlAst`)
2. Expand grammar and macro features in test-backed slices
3. Introduce independent CASL module later, reusing shared semantic interfaces

CASL direction is intentionally separate from traditional SAS because syntax and execution model differ.
