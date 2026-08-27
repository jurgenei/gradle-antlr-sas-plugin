package name.jurgenei.gradle.antlr;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;

/**
 * Registers DS2 XML AST and semantic extraction tasks as independent module.
 */
public final class Ds2GrammarPlugin implements Plugin<Project> {

    /**
     * Creates DS2 grammar plugin.
     */
    public Ds2GrammarPlugin() {
    }

    @Override
    public void apply(final Project project) {
        final TaskProvider<XmlAstDs2GradleTask> xmlAstTask = project.getTasks().register("ds2XmlAst", XmlAstDs2GradleTask.class, task -> {
            task.setGroup("xmlast");
            task.setDescription("Convert DS2 file trees to XML AST output.");
            task.getSourceDirectory().convention(project.getLayout().getProjectDirectory().dir("src/main/ds2"));
            task.getDestinationDirectory().convention(project.getLayout().getBuildDirectory().dir("ds2/xmlast"));
            task.getTargetExtension().convention(".xml");
            task.getContinueOnError().convention(false);
        });

        final TaskProvider<Ds2SemanticExtractGradleTask> semanticTask = project.getTasks().register("ds2Semantic", Ds2SemanticExtractGradleTask.class, task -> {
            task.setGroup("ds2");
            task.setDescription("Extract DS2 semantic JSON summaries from DS2 source files.");
            task.dependsOn(xmlAstTask);
            task.getSourceDirectory().set(xmlAstTask.flatMap(XmlAstDs2GradleTask::getSourceDirectory));
            task.getDestinationDirectory().convention(project.getLayout().getBuildDirectory().dir("ds2/semantic"));
        });

        project.getPlugins().withId("java", plugin -> {
            final JavaPluginExtension javaPluginExtension = project.getExtensions().getByType(JavaPluginExtension.class);
            final SourceSetContainer sourceSets = javaPluginExtension.getSourceSets();
            final SourceSet mainSourceSet = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);

            project.getTasks().withType(XmlAstDs2GradleTask.class).configureEach(task -> {
                task.getRuntimeClasspath().from(mainSourceSet.getRuntimeClasspath());
                task.dependsOn(project.getTasks().named("classes"));
            });
        });

        project.getTasks().register("ds2Pipeline", task -> {
            task.setGroup("ds2");
            task.setDescription("Runs DS2 XML AST and semantic extraction pipeline.");
            task.dependsOn(semanticTask);
        });
    }
}

