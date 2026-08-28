package name.jurgenei.gradle.antlr.semantic;

import java.util.List;

/**
 * DS2-specific semantic summary for early incremental support.
 *
 * @param languageId language identifier.
 * @param sourcePath source-relative path.
 * @param methodNames detected DS2 method names.
 * @param declarationNames detected declaration entries formatted as {@code type name}.
 * @param methodCallEdges detected call edges formatted as {@code caller->callee}.
 * @param procDs2BlockCount number of {@code proc ds2} blocks.
 * @param dataBlockNames detected data-block names.
 * @param packageBlockNames detected package-block names.
 * @param threadBlockNames detected thread-block names.
 * @param setFromSources detected {@code set from} sources.
 */
public record Ds2SemanticProgram(
        String languageId,
        String sourcePath,
        List<String> methodNames,
        List<String> declarationNames,
        List<String> methodCallEdges,
        int procDs2BlockCount,
        List<String> dataBlockNames,
        List<String> packageBlockNames,
        List<String> threadBlockNames,
        List<String> setFromSources) implements SemanticProgram {
}

