# Samples

Each sample applies one plugin id: `name.jurgenei.gradle.antlr.sas`.

`settings.gradle` in each sample uses composite build:

```groovy
pluginManagement {
    includeBuild('../..')
}
```

Run from `gradle-antlr-sas-plugin` directory:

```bash
./gradlew verifySamples
./gradlew -p samples/sas sasMacro sasXmlAst
./gradlew -p samples/casl caslXmlAst caslSemantic
./gradlew -p samples/ds2 ds2XmlAst ds2Semantic
```

Outputs:

- SAS: `samples/sas/src/main/sas` -> `samples/sas/build/output/sas/macro` -> `samples/sas/build/output/sas/xmlast`
- CASL: `samples/casl/src/main/casl` -> `samples/casl/build/output/casl/xmlast` -> `samples/casl/build/output/casl/semantic`
- DS2: `samples/ds2/src/main/ds2` -> `samples/ds2/build/output/ds2/xmlast` and `samples/ds2/build/output/ds2/semantic`

