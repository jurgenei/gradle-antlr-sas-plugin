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
        getGrammar().convention("sas");
        getParserClassName().convention("name.jurgenei.parsers.SasParser");
        getLexerClassName().convention("name.jurgenei.parsers.SasLexer");
        getStartRule().convention("program");
        getIncludes().convention(List.of("**/*.sas"));
    }
}

