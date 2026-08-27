package name.jurgenei.gradle.antlr.semantic;

import java.util.List;

/**
 * Normalized semantic projection shared across language modules.
 *
 * @param languageId language identifier.
 * @param sourcePath source or AST-relative path.
 * @param actionCalls normalized action-call names.
 * @param fedSqlQueryCount number of FEDSQL queries detected in action payloads.
 * @param fedSqlParsedCount number of FEDSQL queries parsed successfully.
 * @param fedSqlFailedCount number of FEDSQL queries failing parser validation.
 * @param fedSqlErrors parse-error summaries for failed FEDSQL payloads.
 */
public record NormalizedSemanticProgram(
        String languageId,
        String sourcePath,
        List<String> actionCalls,
        int fedSqlQueryCount,
        int fedSqlParsedCount,
        int fedSqlFailedCount,
        List<String> fedSqlErrors) implements SemanticProgram {
}

