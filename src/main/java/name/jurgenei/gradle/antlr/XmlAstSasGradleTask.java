package name.jurgenei.gradle.antlr;

import org.gradle.api.model.ObjectFactory;
import org.gradle.work.DisableCachingByDefault;

import javax.inject.Inject;
import java.util.List;

/**
 * SAS-flavored {@link XmlAstGradleTask} with parser defaults preconfigured.
 */
@DisableCachingByDefault(because = "XmlAstGradleTask performs external parser loading and file-system driven conversion not yet declared for safe caching")
public abstract class XmlAstSasGradleTask extends XmlAstGradleTask {

    /**
     * Creates preconfigured SAS XML AST task.
     *
     * @param objects Gradle object factory.
     */
    @Inject
    public XmlAstSasGradleTask(final ObjectFactory objects) {
        super(objects);
        LanguageTaskDefaults.of(
                "sas",
                "name.jurgenei.parsers.SasParser",
                "name.jurgenei.parsers.SasLexer",
                "program",
                List.of("**/*.sas"))
            .applyTo(this);
    }
}

