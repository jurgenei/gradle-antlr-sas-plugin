package name.jurgenei.gradle.antlr;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class CaslGrammarPluginFunctionalTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void runsCaslPipeline() throws Exception {
        final File projectDir = temporaryFolder.newFolder("functional-casl-pipeline");
        writeSettings(projectDir);
        writeBuildFile(projectDir);
        writeCaslSource(projectDir);

        final BuildResult result = run(projectDir, "caslPipeline");
        Assert.assertTrue(result.getOutput().contains("caslXmlAst"));
        Assert.assertTrue(result.getOutput().contains("caslSemantic"));

        final File xmlAst = new File(projectDir, "build/casl/xmlast/program.xml");
        Assert.assertTrue("CASL XML AST file not found", xmlAst.isFile());

        final String xml = Files.readString(xmlAst.toPath(), StandardCharsets.UTF_8);
        Assert.assertTrue(xml.contains("program"));
        Assert.assertTrue(xml.contains("actionRunStatement"));

        final File semantic = new File(projectDir, "build/casl/semantic/program.semantic.json");
        Assert.assertTrue("CASL semantic file not found", semantic.isFile());
        final String semanticJson = Files.readString(semantic.toPath(), StandardCharsets.UTF_8);
        Assert.assertTrue(semanticJson.contains("table.loadTable"));
        Assert.assertTrue(semanticJson.contains("fedSql.execDirect"));
    }

    private static BuildResult run(final File projectDir, final String... args) {
        return GradleRunner.create()
                .withProjectDir(projectDir)
                .withArguments(args)
                .withPluginClasspath()
                .build();
    }

    private static void writeSettings(final File projectDir) throws Exception {
        Files.writeString(
                projectDir.toPath().resolve("settings.gradle"),
                "rootProject.name = 'casl-plugin-functional-test'\n",
                StandardCharsets.UTF_8);
    }

    private static void writeBuildFile(final File projectDir) throws Exception {
        Files.writeString(
                projectDir.toPath().resolve("build.gradle"),
                """
                plugins {
                    id 'java'
                    id 'name.jurgenei.gradle.antlr.casl'
                }
                """,
                StandardCharsets.UTF_8);
    }

    private static void writeCaslSource(final File projectDir) throws Exception {
        final File sourceDir = new File(projectDir, "src/main/casl");
        sourceDir.mkdirs();
        Files.writeString(
                sourceDir.toPath().resolve("program.casl"),
                """
                threshold = 10;
                table.loadTable / caslib='Public', path='cars.sashdat';
                fedSql.execDirect / query='select * from cars';
                if threshold > 5 then run;
                """,
                StandardCharsets.UTF_8);
    }
}

