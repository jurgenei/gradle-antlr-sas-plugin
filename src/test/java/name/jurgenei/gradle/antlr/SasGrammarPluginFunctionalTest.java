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

public class SasGrammarPluginFunctionalTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void expandsMacroFile() throws Exception {
        final File projectDir = temporaryFolder.newFolder("functional-sas-macro");
        writeSettings(projectDir);
        writeBuildFile(projectDir);
        writeSasSource(projectDir);

        run(projectDir, "sasMacro");

        final File expanded = new File(projectDir, "build/sas/macro/program.sas");
        Assert.assertTrue("Expanded SAS file not found", expanded.isFile());

        final String content = Files.readString(expanded.toPath(), StandardCharsets.UTF_8);
        Assert.assertTrue(content.contains("create table work.sales as"));
        Assert.assertFalse(content.contains("%let target_table"));
    }

    @Test
    public void runsMacroToXmlAstPipeline() throws Exception {
        final File projectDir = temporaryFolder.newFolder("functional-sas-pipeline");
        writeSettings(projectDir);
        writeBuildFile(projectDir);
        writeSasSource(projectDir);

        final BuildResult result = run(projectDir, "sasPipeline");
        Assert.assertTrue(result.getOutput().contains("sasMacro"));

        final File xmlAst = new File(projectDir, "build/sas/xmlast/program.xml");
        Assert.assertTrue("XML AST file not found", xmlAst.isFile());

        final String xml = Files.readString(xmlAst.toPath(), StandardCharsets.UTF_8);
        Assert.assertTrue(xml.contains("program"));
    }

    @Test
    public void runsMacroToSexprPipelineWithBeautifiedFormat() throws Exception {
        final File projectDir = temporaryFolder.newFolder("functional-sas-pipeline-sexpr");
        writeSettings(projectDir);
        writeBuildFile(projectDir, """
                tasks.named('sasXmlAst', name.jurgenei.gradle.antlr.XmlAstSasGradleTask) {
                    targetExtension.set('.sexpr')
                    sexprFormat.set('beautified')
                }
                """);
        writeSasSource(projectDir);

        final BuildResult result = run(projectDir, "sasPipeline");
        Assert.assertTrue(result.getOutput().contains("sasMacro"));

        final File sexprAst = new File(projectDir, "build/sas/xmlast/program.sexpr");
        Assert.assertTrue("S-expression AST file not found", sexprAst.isFile());

        final String sexpr = Files.readString(sexprAst.toPath(), StandardCharsets.UTF_8);
        Assert.assertTrue(sexpr.startsWith("(."));
        Assert.assertTrue(sexpr.contains("(ast"));
        Assert.assertTrue(sexpr.contains(System.lineSeparator()));
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
                "rootProject.name = 'sas-plugin-functional-test'\n",
                StandardCharsets.UTF_8);
    }

    private static void writeBuildFile(final File projectDir) throws Exception {
        writeBuildFile(projectDir, "");
    }

    private static void writeBuildFile(final File projectDir, final String extraConfig) throws Exception {
        Files.writeString(
                projectDir.toPath().resolve("build.gradle"),
                """
                plugins {
                    id 'java'
                    id 'name.jurgenei.gradle.antlr.sas'
                }

                %s
                """.formatted(extraConfig),
                StandardCharsets.UTF_8);
    }

    private static void writeSasSource(final File projectDir) throws Exception {
        final File sourceDir = new File(projectDir, "src/main/sas");
        sourceDir.mkdirs();
        Files.writeString(
                sourceDir.toPath().resolve("program.sas"),
                """
                %let target_table = work.sales;
                data work.input;
                  set raw.sales;
                  amount = 42;
                run;

                proc sql;
                  create table &target_table. as
                  select amount from work.input;
                quit;
                """,
                StandardCharsets.UTF_8);
    }
}

