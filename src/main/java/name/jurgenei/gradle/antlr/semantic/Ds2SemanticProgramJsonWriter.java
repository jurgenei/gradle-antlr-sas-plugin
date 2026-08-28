package name.jurgenei.gradle.antlr.semantic;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Writes DS2 semantic summary JSON.
 */
public final class Ds2SemanticProgramJsonWriter {

    /**
     * Creates writer utility.
     */
    public Ds2SemanticProgramJsonWriter() {
    }

    /**
     * Converts DS2 semantic summary to compact JSON string.
     *
     * @param program DS2 semantic program.
     * @return json document.
     */
    public String write(final Ds2SemanticProgram program) {
        final Set<String> uniqueMethods = new LinkedHashSet<>(program.methodNames());
        final Set<String> uniqueDeclarations = new LinkedHashSet<>(program.declarationNames());
        final Set<String> uniqueCallEdges = new LinkedHashSet<>(program.methodCallEdges());
        final Set<String> uniqueDataBlocks = new LinkedHashSet<>(program.dataBlockNames());
        final Set<String> uniquePackageBlocks = new LinkedHashSet<>(program.packageBlockNames());
        final Set<String> uniqueThreadBlocks = new LinkedHashSet<>(program.threadBlockNames());
        final Set<String> uniqueSetFromSources = new LinkedHashSet<>(program.setFromSources());
        final StringBuilder methods = new StringBuilder();
        final StringBuilder declarations = new StringBuilder();
        final StringBuilder callEdges = new StringBuilder();
        final StringBuilder dataBlocks = new StringBuilder();
        final StringBuilder packageBlocks = new StringBuilder();
        final StringBuilder threadBlocks = new StringBuilder();
        final StringBuilder setFromSources = new StringBuilder();
        int index = 0;
        for (String method : uniqueMethods) {
            if (index++ > 0) {
                methods.append(',');
            }
            methods.append('"').append(escape(method)).append('"');
        }
        appendQuotedArray(uniqueDeclarations, declarations);
        appendQuotedArray(uniqueCallEdges, callEdges);
        appendQuotedArray(uniqueDataBlocks, dataBlocks);
        appendQuotedArray(uniquePackageBlocks, packageBlocks);
        appendQuotedArray(uniqueThreadBlocks, threadBlocks);
        appendQuotedArray(uniqueSetFromSources, setFromSources);

        return "{\n"
                + "  \"language\": \"" + escape(program.languageId()) + "\",\n"
                + "  \"sourcePath\": \"" + escape(program.sourcePath()) + "\",\n"
                + "  \"methodCount\": " + program.methodNames().size() + ",\n"
                + "  \"methods\": [" + methods + "],\n"
                + "  \"declarationCount\": " + program.declarationNames().size() + ",\n"
                + "  \"declarations\": [" + declarations + "],\n"
                + "  \"callEdgeCount\": " + program.methodCallEdges().size() + ",\n"
                + "  \"callEdges\": [" + callEdges + "],\n"
                + "  \"procDs2BlockCount\": " + program.procDs2BlockCount() + ",\n"
                + "  \"dataBlocks\": [" + dataBlocks + "],\n"
                + "  \"packageBlocks\": [" + packageBlocks + "],\n"
                + "  \"threadBlocks\": [" + threadBlocks + "],\n"
                + "  \"setFromSources\": [" + setFromSources + "]\n"
                + "}\n";
    }

    private void appendQuotedArray(final Set<String> values, final StringBuilder out) {
        int idx = 0;
        for (String value : values) {
            if (idx++ > 0) {
                out.append(',');
            }
            out.append('"').append(escape(value)).append('"');
        }
    }

    private String escape(final String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}

