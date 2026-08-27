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
 * Regression harness for DS2 pipeline fixtures.
 */
public class Ds2RegressionFunctionalTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void executesAllDs2RegressionCases() throws Exception {
        final URL resource = getClass().getClassLoader().getResource("regression-ds2");
        Assert.assertNotNull("Missing DS2 regression fixtures", resource);

        final Path root = Path.of(resource.toURI());
        final List<Path> cases = Files.list(root)
                .filter(Files::isDirectory)
                .sorted()
                .collect(Collectors.toList());
        Assert.assertFalse("No DS2 regression cases found", cases.isEmpty());

        for (Path caseDir : cases) {
            runCase(caseDir);
        }
    }

    private void runCase(final Path caseDir) throws Exception {
        final File projectDir = temporaryFolder.newFolder(caseDir.getFileName().toString());
        Files.writeString(projectDir.toPath().resolve("settings.gradle"), "rootProject.name = 'ds2-regression'\n", StandardCharsets.UTF_8);
        Files.writeString(projectDir.toPath().resolve("build.gradle"), """
                plugins {
                    id 'java'
                    id 'name.jurgenei.gradle.antlr.ds2'
                }
                """, StandardCharsets.UTF_8);

        final Path inputDir = caseDir.resolve("input");
        final Path expectedDir = caseDir.resolve("expected");
        final Path projectInput = projectDir.toPath().resolve("src/main/ds2");
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
                .withArguments("ds2Pipeline")
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
            final Path xmlPath = projectDir.toPath().resolve("build/ds2/xmlast")
                    .resolve(relative.getParent() == null ? Path.of(xmlName) : relative.getParent().resolve(xmlName));
            Assert.assertTrue("Missing DS2 XML AST file for " + caseDir.getFileName(), Files.isRegularFile(xmlPath));

            final String semanticName = (dot > 0 ? fileName.substring(0, dot) : fileName) + ".semantic.json";
            final Path semanticPath = projectDir.toPath().resolve("build/ds2/semantic")
                    .resolve(relative.getParent() == null ? Path.of(semanticName) : relative.getParent().resolve(semanticName));
            Assert.assertTrue("Missing DS2 semantic file for " + caseDir.getFileName(), Files.isRegularFile(semanticPath));
            final String semanticJson = Files.readString(semanticPath, StandardCharsets.UTF_8);
            Assert.assertTrue("Missing procDs2BlockCount field", semanticJson.contains("\"procDs2BlockCount\": 1"));

            if (caseDir.getFileName().toString().contains("method")) {
                Assert.assertTrue("Missing run method", semanticJson.contains("\"run\""));
            }
            if (caseDir.getFileName().toString().contains("declarations")) {
                Assert.assertTrue("Missing declaration count", semanticJson.contains("\"declarationCount\": 2"));
                Assert.assertTrue("Missing declaration value", semanticJson.contains("\"int amount\""));
            }
            if (caseDir.getFileName().toString().contains("thread-package")) {
                Assert.assertTrue("Missing package metadata", semanticJson.contains("\"packageBlocks\": [\"work.pkg\"]"));
                Assert.assertTrue("Missing thread metadata", semanticJson.contains("\"threadBlocks\": [\"work.worker\"]"));
                Assert.assertTrue("Missing set-from metadata", semanticJson.contains("\"setFromSources\": [\"work.source\"]"));
                Assert.assertTrue("Missing local call edge", semanticJson.contains("\"run->helper\""));
                Assert.assertTrue("Missing qualified call edge", semanticJson.contains("\"run->worker.execute\""));
            }
        }
    }
}

