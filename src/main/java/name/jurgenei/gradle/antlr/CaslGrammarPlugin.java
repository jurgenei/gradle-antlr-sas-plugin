package name.jurgenei.gradle.antlr;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;

/**
 * Registers CASL XML AST tasks as independent module from traditional SAS pipeline.
 */
public final class CaslGrammarPlugin implements Plugin<Project> {

    /**
     * Creates CASL grammar plugin.
     */
    public CaslGrammarPlugin() {
    }

    @Override
    public void apply(final Project project) {
        final TaskProvider<XmlAstCaslGradleTask> xmlAstTask = project.getTasks().register("caslXmlAst", XmlAstCaslGradleTask.class, task -> {
            task.setGroup("xmlast");
            task.setDescription("Convert CASL file trees to XML AST output.");
            task.getSourceDirectory().convention(project.getLayout().getProjectDirectory().dir("src/main/casl"));
            task.getDestinationDirectory().convention(project.getLayout().getBuildDirectory().dir("casl/xmlast"));
            task.getTargetExtension().convention(".xml");
            task.getContinueOnError().convention(false);
        });

        final TaskProvider<CaslSemanticExtractGradleTask> semanticTask = project.getTasks().register("caslSemantic", CaslSemanticExtractGradleTask.class, task -> {
            task.setGroup("casl");
            task.setDescription("Extract normalized semantic JSON from CASL XML AST output.");
            task.dependsOn(xmlAstTask);
            task.getSourceDirectory().set(xmlAstTask.flatMap(XmlAstCaslGradleTask::getDestinationDirectory));
            task.getCaslSourceDirectory().set(xmlAstTask.flatMap(XmlAstCaslGradleTask::getSourceDirectory));
            task.getDestinationDirectory().convention(project.getLayout().getBuildDirectory().dir("casl/semantic"));
        });

        project.getPlugins().withId("java", plugin -> {
            final JavaPluginExtension javaPluginExtension = project.getExtensions().getByType(JavaPluginExtension.class);
            final SourceSetContainer sourceSets = javaPluginExtension.getSourceSets();
            final SourceSet mainSourceSet = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);

            project.getTasks().withType(XmlAstCaslGradleTask.class).configureEach(task -> {
                task.getRuntimeClasspath().from(mainSourceSet.getRuntimeClasspath());
                task.dependsOn(project.getTasks().named("classes"));
            });
        });

        project.getTasks().register("caslPipeline", task -> {
            task.setGroup("casl");
            task.setDescription("Runs CASL XML AST and semantic extraction pipeline.");
            task.dependsOn(semanticTask);
        });
    }
}

