package name.jurgenei.gradle.antlr;

import org.gradle.api.model.ObjectFactory;
import org.gradle.work.DisableCachingByDefault;

import javax.inject.Inject;
import java.util.List;

/**
 * DS2-flavored {@link XmlAstGradleTask} with parser defaults preconfigured.
 */
@DisableCachingByDefault(because = "XmlAstGradleTask performs external parser loading and file-system driven conversion not yet declared for safe caching")
public abstract class XmlAstDs2GradleTask extends XmlAstGradleTask {

    /**
     * Creates preconfigured DS2 XML AST task.
     *
     * @param objects Gradle object factory.
     */
    @Inject
    public XmlAstDs2GradleTask(final ObjectFactory objects) {
        super(objects);
        getGrammar().convention("ds2");
        getParserClassName().convention("name.jurgenei.parsers.Ds2Parser");
        getLexerClassName().convention("name.jurgenei.parsers.Ds2Lexer");
        getStartRule().convention("program");
        getIncludes().convention(List.of("**/*.ds2", "**/*.sas"));
    }
}

