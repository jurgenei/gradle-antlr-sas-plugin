package name.jurgenei.gradle.antlr;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;

/**
 * Registers SAS, CASL, and DS2 pipelines under single plugin id.
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

        final TaskProvider<XmlAstCaslGradleTask> caslXmlAstTask = project.getTasks().register("caslXmlAst", XmlAstCaslGradleTask.class, task -> {
            task.setGroup("xmlast");
            task.setDescription("Convert CASL file trees to XML AST output.");
            task.getSourceDirectory().convention(project.getLayout().getProjectDirectory().dir("src/main/casl"));
            task.getDestinationDirectory().convention(project.getLayout().getBuildDirectory().dir("casl/xmlast"));
            task.getTargetExtension().convention(".xml");
            task.getContinueOnError().convention(false);
        });

        final TaskProvider<CaslSemanticExtractGradleTask> caslSemanticTask = project.getTasks().register("caslSemantic", CaslSemanticExtractGradleTask.class, task -> {
            task.setGroup("casl");
            task.setDescription("Extract normalized semantic JSON from CASL XML AST output.");
            task.dependsOn(caslXmlAstTask);
            task.getSourceDirectory().set(caslXmlAstTask.flatMap(XmlAstCaslGradleTask::getDestinationDirectory));
            task.getCaslSourceDirectory().set(caslXmlAstTask.flatMap(XmlAstCaslGradleTask::getSourceDirectory));
            task.getDestinationDirectory().convention(project.getLayout().getBuildDirectory().dir("casl/semantic"));
        });

        final TaskProvider<XmlAstDs2GradleTask> ds2XmlAstTask = project.getTasks().register("ds2XmlAst", XmlAstDs2GradleTask.class, task -> {
            task.setGroup("xmlast");
            task.setDescription("Convert DS2 file trees to XML AST output.");
            task.getSourceDirectory().convention(project.getLayout().getProjectDirectory().dir("src/main/ds2"));
            task.getDestinationDirectory().convention(project.getLayout().getBuildDirectory().dir("ds2/xmlast"));
            task.getTargetExtension().convention(".xml");
            task.getContinueOnError().convention(false);
        });

        final TaskProvider<Ds2SemanticExtractGradleTask> ds2SemanticTask = project.getTasks().register("ds2Semantic", Ds2SemanticExtractGradleTask.class, task -> {
            task.setGroup("ds2");
            task.setDescription("Extract DS2 semantic JSON summaries from DS2 source files.");
            task.dependsOn(ds2XmlAstTask);
            task.getSourceDirectory().set(ds2XmlAstTask.flatMap(XmlAstDs2GradleTask::getSourceDirectory));
            task.getDestinationDirectory().convention(project.getLayout().getBuildDirectory().dir("ds2/semantic"));
        });

        project.getPlugins().withId("java", plugin -> {
            final JavaPluginExtension javaPluginExtension = project.getExtensions().getByType(JavaPluginExtension.class);
            final SourceSetContainer sourceSets = javaPluginExtension.getSourceSets();
            final SourceSet mainSourceSet = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);

            project.getTasks().withType(XmlAstSasGradleTask.class).configureEach(task -> {
                task.getRuntimeClasspath().from(mainSourceSet.getRuntimeClasspath());
                task.dependsOn(project.getTasks().named("classes"));
            });

            project.getTasks().withType(XmlAstCaslGradleTask.class).configureEach(task -> {
                task.getRuntimeClasspath().from(mainSourceSet.getRuntimeClasspath());
                task.dependsOn(project.getTasks().named("classes"));
            });

            project.getTasks().withType(XmlAstDs2GradleTask.class).configureEach(task -> {
                task.getRuntimeClasspath().from(mainSourceSet.getRuntimeClasspath());
                task.dependsOn(project.getTasks().named("classes"));
            });
        });

        project.getTasks().register("sasPipeline", task -> {
            task.setGroup("sas");
            task.setDescription("Runs macro expansion and XML AST generation for SAS sources.");
            task.dependsOn(xmlAstTask);
        });

        project.getTasks().register("caslPipeline", task -> {
            task.setGroup("casl");
            task.setDescription("Runs CASL XML AST and semantic extraction pipeline.");
            task.dependsOn(caslSemanticTask);
        });

        project.getTasks().register("ds2Pipeline", task -> {
            task.setGroup("ds2");
            task.setDescription("Runs DS2 XML AST and semantic extraction pipeline.");
            task.dependsOn(ds2SemanticTask);
        });
    }
}

