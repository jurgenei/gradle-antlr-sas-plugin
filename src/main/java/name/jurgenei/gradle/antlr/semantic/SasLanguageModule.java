package name.jurgenei.gradle.antlr.semantic;

/**
 * Shared semantic descriptor for traditional SAS module.
 */
public final class SasLanguageModule implements LanguageModule {

    /**
     * Creates descriptor.
     */
    public SasLanguageModule() {
    }

    @Override
    public String languageId() {
        return "sas";
    }

    @Override
    public String startRule() {
        return "program";
    }
}

