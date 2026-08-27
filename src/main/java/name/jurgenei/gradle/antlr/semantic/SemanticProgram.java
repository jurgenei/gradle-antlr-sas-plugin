package name.jurgenei.gradle.antlr.semantic;

/**
 * Minimal semantic result contract shared by language modules.
 */
public interface SemanticProgram {

    /**
     * Returns language id that produced semantic model.
     *
     * @return language identifier.
     */
    String languageId();

    /**
     * Returns source location used to produce semantic model.
     *
     * @return source path.
     */
    String sourcePath();
}

