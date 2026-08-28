package name.jurgenei.gradle.antlr;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;
import java.util.stream.Collectors;

public class Ds2GrammarPluginTest {

    @Test
    public void registersDs2Tasks() {
        final Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("java");

        new SasGrammarPlugin().apply(project);

        Assert.assertNotNull(project.getTasks().findByName("ds2XmlAst"));
        Assert.assertNotNull(project.getTasks().findByName("ds2Semantic"));
        Assert.assertNotNull(project.getTasks().findByName("ds2Pipeline"));
        XmlAstDs2GradleTask.class.cast(project.getTasks().getByName("ds2XmlAst"));
        Ds2SemanticExtractGradleTask.class.cast(project.getTasks().getByName("ds2Semantic"));
    }

    @Test
    public void configuresDs2Defaults() {
        final Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("java");

        new SasGrammarPlugin().apply(project);

        final XmlAstDs2GradleTask xmlTask = XmlAstDs2GradleTask.class.cast(project.getTasks().getByName("ds2XmlAst"));
        Assert.assertEquals("ds2", xmlTask.getGrammar().get());
        Assert.assertEquals("name.jurgenei.parsers.Ds2Parser", xmlTask.getParserClassName().get());
        Assert.assertEquals("name.jurgenei.parsers.Ds2Lexer", xmlTask.getLexerClassName().get());
        Assert.assertEquals("program", xmlTask.getStartRule().get());

        final Task pipeline = project.getTasks().getByName("ds2Pipeline");
        final TaskDependency dependencies = pipeline.getTaskDependencies();
        final Set<String> dependencyNames = dependencies.getDependencies(pipeline)
                .stream()
                .map(Task::getName)
                .collect(Collectors.toSet());
        Assert.assertTrue(dependencyNames.contains("ds2Semantic"));
    }
}

