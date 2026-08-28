package name.jurgenei.gradle.antlr;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;
import java.util.stream.Collectors;

public class CaslGrammarPluginTest {

    @Test
    public void registersCaslTasks() {
        final Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("java");

        new SasGrammarPlugin().apply(project);

        Assert.assertNotNull(project.getTasks().findByName("caslXmlAst"));
        Assert.assertNotNull(project.getTasks().findByName("caslSemantic"));
        Assert.assertNotNull(project.getTasks().findByName("caslPipeline"));
        XmlAstCaslGradleTask.class.cast(project.getTasks().getByName("caslXmlAst"));
        CaslSemanticExtractGradleTask.class.cast(project.getTasks().getByName("caslSemantic"));
    }

    @Test
    public void configuresCaslDefaults() {
        final Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("java");

        new SasGrammarPlugin().apply(project);

        final XmlAstCaslGradleTask task = XmlAstCaslGradleTask.class.cast(project.getTasks().getByName("caslXmlAst"));
        Assert.assertEquals("casl", task.getGrammar().get());
        Assert.assertEquals("name.jurgenei.parsers.CaslParser", task.getParserClassName().get());
        Assert.assertEquals("name.jurgenei.parsers.CaslLexer", task.getLexerClassName().get());
        Assert.assertEquals("program", task.getStartRule().get());
        Assert.assertTrue(task.getIncludes().get().contains("**/*.casl"));

        final Task pipeline = project.getTasks().getByName("caslPipeline");
        final TaskDependency dependencies = pipeline.getTaskDependencies();
        final Set<String> dependencyNames = dependencies.getDependencies(pipeline)
                .stream()
                .map(Task::getName)
                .collect(Collectors.toSet());
        Assert.assertTrue(dependencyNames.contains("caslSemantic"));
    }
}

