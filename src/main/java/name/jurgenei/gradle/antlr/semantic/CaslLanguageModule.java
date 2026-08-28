package name.jurgenei.gradle.antlr.semantic;

/**
 * Shared semantic descriptor for CASL module.
 */
public final class CaslLanguageModule implements LanguageModule {

    /**
     * Creates descriptor.
     */
    public CaslLanguageModule() {
    }

    @Override
    public String languageId() {
        return "casl";
    }

    @Override
    public String startRule() {
        return "program";
    }
}

