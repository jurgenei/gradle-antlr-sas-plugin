package name.jurgenei.gradle.antlr;

import org.gradle.testkit.runner.GradleRunner;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Regression harness for CASL pipeline fixtures.
 */
public class CaslRegressionFunctionalTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void executesAllCaslRegressionCases() throws Exception {
        final URL resource = getClass().getClassLoader().getResource("regression-casl");
        Assert.assertNotNull("Missing CASL regression fixtures", resource);

        final Path root = Path.of(resource.toURI());
        final List<Path> cases = Files.list(root)
                .filter(Files::isDirectory)
                .sorted()
                .collect(Collectors.toList());
        Assert.assertFalse("No CASL regression cases found", cases.isEmpty());

        for (Path caseDir : cases) {
            runCase(caseDir);
        }
    }

    private void runCase(final Path caseDir) throws Exception {
        final File projectDir = temporaryFolder.newFolder(caseDir.getFileName().toString());
        Files.writeString(projectDir.toPath().resolve("settings.gradle"), "rootProject.name = 'casl-regression'\n", StandardCharsets.UTF_8);
        Files.writeString(projectDir.toPath().resolve("build.gradle"), """
                plugins {
                    id 'java'
                    id 'name.jurgenei.gradle.antlr.casl'
                }
                """, StandardCharsets.UTF_8);

        final Path inputDir = caseDir.resolve("input");
        final Path expectedDir = caseDir.resolve("expected");
        final Path projectInput = projectDir.toPath().resolve("src/main/casl");
        Files.createDirectories(projectInput);

        final List<Path> inputFiles = Files.walk(inputDir)
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        for (Path inputFile : inputFiles) {
            final Path relative = inputDir.relativize(inputFile);
            final Path target = projectInput.resolve(relative);
            Files.createDirectories(target.getParent());
            Files.copy(inputFile, target);
        }

        GradleRunner.create()
                .withProjectDir(projectDir)
                .withArguments("caslPipeline")
                .withPluginClasspath()
                .build();

        for (Path inputFile : inputFiles) {
            final Path relative = inputDir.relativize(inputFile);

            final Path expectedFile = expectedDir.resolve(relative);
            final Path actualFile = projectInput.resolve(relative);
            final String expected = Files.readString(expectedFile, StandardCharsets.UTF_8).trim();
            final String actual = Files.readString(actualFile, StandardCharsets.UTF_8).trim();
            Assert.assertEquals("Input mismatch for " + caseDir.getFileName(), expected, actual);

            final String fileName = relative.getFileName().toString();
            final int dot = fileName.lastIndexOf('.');
            final String xmlName = (dot > 0 ? fileName.substring(0, dot) : fileName) + ".xml";
            final Path xmlPath = projectDir.toPath().resolve("build/casl/xmlast")
                    .resolve(relative.getParent() == null ? Path.of(xmlName) : relative.getParent().resolve(xmlName));
            Assert.assertTrue("Missing CASL XML AST file for " + caseDir.getFileName(), Files.isRegularFile(xmlPath));

            final String semanticName = (dot > 0 ? fileName.substring(0, dot) : fileName) + ".semantic.json";
            final Path semanticPath = projectDir.toPath().resolve("build/casl/semantic")
                    .resolve(relative.getParent() == null ? Path.of(semanticName) : relative.getParent().resolve(semanticName));
            Assert.assertTrue("Missing CASL semantic file for " + caseDir.getFileName(), Files.isRegularFile(semanticPath));
            final String semanticJson = Files.readString(semanticPath, StandardCharsets.UTF_8);

            if (caseDir.getFileName().toString().contains("actions") || caseDir.getFileName().toString().contains("mixed")) {
                Assert.assertTrue("Missing table.loadTable semantic action", semanticJson.contains("table.loadTable"));
                Assert.assertTrue("Missing fedSql.execDirect semantic action", semanticJson.contains("fedSql.execDirect"));
            }

            if (caseDir.getFileName().toString().contains("fedsql") || caseDir.getFileName().toString().contains("mixed")) {
                Assert.assertTrue("Missing FEDSQL query counter", semanticJson.contains("\"fedSqlQueryCount\":"));
                Assert.assertTrue("Missing FEDSQL parsed counter", semanticJson.contains("\"fedSqlParsedCount\":"));
                Assert.assertTrue("Expected no FEDSQL parse failures", semanticJson.contains("\"fedSqlFailedCount\": 0"));
            }
        }
    }
}

