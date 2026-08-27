package name.jurgenei.gradle.antlr;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;

/**
 * Registers SAS macro-expansion and XML AST tasks wired as chained pipeline.
 */
public final class SasGrammarPlugin implements Plugin<Project> {

    /**
     * Creates SAS grammar plugin.
     */
    public SasGrammarPlugin() {
    }

    @Override
    public void apply(final Project project) {
        final TaskProvider<SasMacroGradleTask> macroTask = project.getTasks().register("sasMacro", SasMacroGradleTask.class, task -> {
            task.setGroup("sas");
            task.setDescription("Expand SAS macros into intermediate SAS sources.");
        });

        final TaskProvider<XmlAstSasGradleTask> xmlAstTask = project.getTasks().register("sasXmlAst", XmlAstSasGradleTask.class, task -> {
            task.setGroup("xmlast");
            task.setDescription("Convert macro-expanded SAS sources to XML AST output.");
            task.dependsOn(macroTask);
            task.getSourceDirectory().set(macroTask.flatMap(SasMacroGradleTask::getDestinationDirectory));
            task.getDestinationDirectory().convention(project.getLayout().getBuildDirectory().dir("sas/xmlast"));
            task.getTargetExtension().convention(".xml");
            task.getContinueOnError().convention(false);
        });

        project.getPlugins().withId("java", plugin -> {
            final JavaPluginExtension javaPluginExtension = project.getExtensions().getByType(JavaPluginExtension.class);
            final SourceSetContainer sourceSets = javaPluginExtension.getSourceSets();
            final SourceSet mainSourceSet = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);

            project.getTasks().withType(XmlAstSasGradleTask.class).configureEach(task -> {
                task.getRuntimeClasspath().from(mainSourceSet.getRuntimeClasspath());
                task.dependsOn(project.getTasks().named("classes"));
            });
        });

        project.getTasks().register("sasPipeline", task -> {
            task.setGroup("sas");
            task.setDescription("Runs macro expansion and XML AST generation for SAS sources.");
            task.dependsOn(xmlAstTask);
        });
    }
}

