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
 * Regression harness for SAS pipeline fixtures.
 */
public class SasRegressionFunctionalTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void executesAllRegressionCases() throws Exception {
        final URL resource = getClass().getClassLoader().getResource("regression");
        Assert.assertNotNull("Missing regression fixtures", resource);

        final Path root = Path.of(resource.toURI());
        final List<Path> cases = Files.list(root)
                .filter(Files::isDirectory)
                .sorted()
                .collect(Collectors.toList());
        Assert.assertFalse("No regression cases found", cases.isEmpty());

        for (Path caseDir : cases) {
            runCase(caseDir);
        }
    }

    private void runCase(final Path caseDir) throws Exception {
        final File projectDir = temporaryFolder.newFolder(caseDir.getFileName().toString());
        Files.writeString(projectDir.toPath().resolve("settings.gradle"), "rootProject.name = 'sas-regression'\n", StandardCharsets.UTF_8);
        Files.writeString(projectDir.toPath().resolve("build.gradle"), """
                plugins {
                    id 'java'
                    id 'name.jurgenei.gradle.antlr.sas'
                }
                """, StandardCharsets.UTF_8);

        final Path inputDir = caseDir.resolve("input");
        final Path expectedDir = caseDir.resolve("expected");
        final Path projectInput = projectDir.toPath().resolve("src/main/sas");
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
                .withArguments("sasPipeline")
                .withPluginClasspath()
                .build();

        for (Path inputFile : inputFiles) {
            final Path relative = inputDir.relativize(inputFile);

            final Path expectedMacroFile = expectedDir.resolve(relative);
            final Path actualMacroFile = projectDir.toPath().resolve("build/sas/macro").resolve(relative);
            Assert.assertTrue("Missing expanded file for " + caseDir.getFileName(), Files.isRegularFile(actualMacroFile));

            final String expectedMacro = Files.readString(expectedMacroFile, StandardCharsets.UTF_8).trim();
            final String actualMacro = Files.readString(actualMacroFile, StandardCharsets.UTF_8).trim();
            Assert.assertEquals("Macro expansion mismatch for " + caseDir.getFileName(), expectedMacro, actualMacro);

            final String fileName = relative.getFileName().toString();
            final int dot = fileName.lastIndexOf('.');
            final String xmlName = (dot > 0 ? fileName.substring(0, dot) : fileName) + ".xml";
            final Path xmlPath = projectDir.toPath().resolve("build/sas/xmlast").resolve(relative.getParent() == null ? Path.of(xmlName) : relative.getParent().resolve(xmlName));
            Assert.assertTrue("Missing XML AST file for " + caseDir.getFileName(), Files.isRegularFile(xmlPath));
        }
    }
}

