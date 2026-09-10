package name.jurgenei.gradle.antlr;

import org.gradle.api.model.ObjectFactory;
import org.gradle.work.DisableCachingByDefault;

import javax.inject.Inject;
import java.util.List;

/**
 * CASL-flavored {@link XmlAstGradleTask} with parser defaults preconfigured.
 */
@DisableCachingByDefault(because = "XmlAstGradleTask performs external parser loading and file-system driven conversion not yet declared for safe caching")
public abstract class XmlAstCaslGradleTask extends XmlAstGradleTask {

    /**
     * Creates preconfigured CASL XML AST task.
     *
     * @param objects Gradle object factory.
     */
    @Inject
    public XmlAstCaslGradleTask(final ObjectFactory objects) {
        super(objects);
        LanguageTaskDefaults.of(
                "casl",
                "name.jurgenei.parsers.CaslParser",
                "name.jurgenei.parsers.CaslLexer",
                "program",
                List.of("**/*.casl", "**/*.cas"))
            .applyTo(this);
    }
}

