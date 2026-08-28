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

public class Ds2GrammarPluginFunctionalTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void runsDs2Pipeline() throws Exception {
        final File projectDir = temporaryFolder.newFolder("functional-ds2-pipeline");
        writeSettings(projectDir);
        writeBuildFile(projectDir);
        writeDs2Source(projectDir);

        final BuildResult result = run(projectDir, "ds2Pipeline");
        Assert.assertTrue(result.getOutput().contains("ds2XmlAst"));
        Assert.assertTrue(result.getOutput().contains("ds2Semantic"));

        final File xmlAst = new File(projectDir, "build/ds2/xmlast/program.xml");
        Assert.assertTrue("DS2 XML AST file not found", xmlAst.isFile());

        final File semantic = new File(projectDir, "build/ds2/semantic/program.semantic.json");
        Assert.assertTrue("DS2 semantic file not found", semantic.isFile());
        final String semanticJson = Files.readString(semantic.toPath(), StandardCharsets.UTF_8);
        Assert.assertTrue(semanticJson.contains("\"methodCount\": 2"));
        Assert.assertTrue(semanticJson.contains("\"run\""));
        Assert.assertTrue(semanticJson.contains("\"helper\""));
        Assert.assertTrue(semanticJson.contains("\"declarationCount\": 1"));
        Assert.assertTrue(semanticJson.contains("\"int x\""));
        Assert.assertTrue(semanticJson.contains("\"setFromSources\": [\"work.input\"]"));
        Assert.assertTrue(semanticJson.contains("\"run->helper\""));
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
                "rootProject.name = 'ds2-plugin-functional-test'\n",
                StandardCharsets.UTF_8);
    }

    private static void writeBuildFile(final File projectDir) throws Exception {
        Files.writeString(
                projectDir.toPath().resolve("build.gradle"),
                """
                plugins {
                    id 'java'
                    id 'name.jurgenei.gradle.antlr.sas'
                }
                """,
                StandardCharsets.UTF_8);
    }

    private static void writeDs2Source(final File projectDir) throws Exception {
        final File sourceDir = new File(projectDir, "src/main/ds2");
        sourceDir.mkdirs();
        Files.writeString(
                sourceDir.toPath().resolve("program.ds2"),
                """
                proc ds2;
                data work.demo;
                  dcl int x;
                  set from work.input;
                  method run();
                    helper();
                  endmethod;
                  method helper(varchar name);
                    x = 1;
                  endmethod;
                enddata;
                run;
                quit;
                """,
                StandardCharsets.UTF_8);
    }
}

