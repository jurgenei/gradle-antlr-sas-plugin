package name.jurgenei.gradle.antlr.semantic;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Writes normalized semantic program as deterministic JSON.
 */
public final class SemanticProgramJsonWriter {

    /**
     * Creates writer utility.
     */
    public SemanticProgramJsonWriter() {
    }

    /**
     * Converts semantic program to compact JSON string.
     *
     * @param program normalized semantic program.
     * @return json document.
     */
    public String write(final NormalizedSemanticProgram program) {
        final Set<String> uniqueActions = new LinkedHashSet<>(program.actionCalls());
        final StringBuilder actions = new StringBuilder();
        final StringBuilder fedSqlErrors = new StringBuilder();
        int index = 0;
        for (String action : uniqueActions) {
            if (index++ > 0) {
                actions.append(',');
            }
            actions.append('"').append(escape(action)).append('"');
        }
        int errorIndex = 0;
        for (String fedSqlError : program.fedSqlErrors()) {
            if (errorIndex++ > 0) {
                fedSqlErrors.append(',');
            }
            fedSqlErrors.append('"').append(escape(fedSqlError)).append('"');
        }

        return "{\n"
                + "  \"language\": \"" + escape(program.languageId()) + "\",\n"
                + "  \"sourcePath\": \"" + escape(program.sourcePath()) + "\",\n"
                + "  \"actionCount\": " + program.actionCalls().size() + ",\n"
                + "  \"actions\": [" + actions + "],\n"
                + "  \"fedSqlQueryCount\": " + program.fedSqlQueryCount() + ",\n"
                + "  \"fedSqlParsedCount\": " + program.fedSqlParsedCount() + ",\n"
                + "  \"fedSqlFailedCount\": " + program.fedSqlFailedCount() + ",\n"
                + "  \"fedSqlErrors\": [" + fedSqlErrors + "]\n"
                + "}\n";
    }

    private String escape(final String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}

