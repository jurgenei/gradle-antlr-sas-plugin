package name.jurgenei.gradle.antlr;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;
import java.util.stream.Collectors;

public class SasGrammarPluginTest {

    @Test
    public void registersPipelineTasks() {
        final Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("java");

        new SasGrammarPlugin().apply(project);

        Assert.assertNotNull(project.getTasks().findByName("sasMacro"));
        Assert.assertNotNull(project.getTasks().findByName("sasXmlAst"));
        Assert.assertNotNull(project.getTasks().findByName("sasPipeline"));
        Assert.assertNotNull(project.getTasks().findByName("caslXmlAst"));
        Assert.assertNotNull(project.getTasks().findByName("caslSemantic"));
        Assert.assertNotNull(project.getTasks().findByName("caslPipeline"));
        Assert.assertNotNull(project.getTasks().findByName("ds2XmlAst"));
        Assert.assertNotNull(project.getTasks().findByName("ds2Semantic"));
        Assert.assertNotNull(project.getTasks().findByName("ds2Pipeline"));
        SasMacroGradleTask.class.cast(project.getTasks().getByName("sasMacro"));
        XmlAstSasGradleTask.class.cast(project.getTasks().getByName("sasXmlAst"));
        XmlAstCaslGradleTask.class.cast(project.getTasks().getByName("caslXmlAst"));
        CaslSemanticExtractGradleTask.class.cast(project.getTasks().getByName("caslSemantic"));
        XmlAstDs2GradleTask.class.cast(project.getTasks().getByName("ds2XmlAst"));
        Ds2SemanticExtractGradleTask.class.cast(project.getTasks().getByName("ds2Semantic"));
    }

    @Test
    public void configuresXmlAstDefaultsAndChain() {
        final Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("java");

        new SasGrammarPlugin().apply(project);

        final XmlAstSasGradleTask xmlTask = XmlAstSasGradleTask.class.cast(project.getTasks().getByName("sasXmlAst"));
        Assert.assertEquals("sas", xmlTask.getGrammar().get());
        Assert.assertEquals("name.jurgenei.parsers.SasParser", xmlTask.getParserClassName().get());
        Assert.assertEquals("name.jurgenei.parsers.SasLexer", xmlTask.getLexerClassName().get());
        Assert.assertEquals("program", xmlTask.getStartRule().get());

        final TaskDependency dependencies = xmlTask.getTaskDependencies();
        final Set<String> dependencyNames = dependencies.getDependencies(xmlTask)
                .stream()
                .map(Task::getName)
                .collect(Collectors.toSet());

        Assert.assertTrue(dependencyNames.contains("sasMacro"));
    }
}

