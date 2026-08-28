package name.jurgenei.gradle.antlr.semantic;

/**
 * Declares common language-module identity shared by SAS-family modules.
 */
public interface LanguageModule {

    /**
     * Returns stable language identifier (for example {@code sas} or {@code casl}).
     *
     * @return language identifier.
     */
    String languageId();

    /**
     * Returns parser entry rule for module.
     *
     * @return start rule name.
     */
    String startRule();
}

