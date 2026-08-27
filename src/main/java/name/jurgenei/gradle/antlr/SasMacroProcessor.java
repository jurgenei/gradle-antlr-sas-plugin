package name.jurgenei.gradle.antlr;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Expands a minimal subset of SAS macro syntax for proof-of-concept builds.
 *
 * <p>Supported behavior:</p>
 * <ul>
 *     <li>{@code %let name = value;} declarations.</li>
 *     <li>{@code &name} and {@code &name.} references.</li>
 *     <li>Recursive expansion of macro values with loop protection.</li>
 * </ul>
 */
public final class SasMacroProcessor {

    private static final Pattern LET_PATTERN = Pattern.compile("(?i)%let\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*=\\s*(.*?);");
    private static final Pattern MACRO_DEF_PATTERN = Pattern.compile("(?is)%macro\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*;(.*?)%mend\\s*(?:[A-Za-z_][A-Za-z0-9_]*)?\\s*;");
    private static final Pattern MACRO_CALL_PATTERN = Pattern.compile("(?i)%([A-Za-z_][A-Za-z0-9_]*)\\s*;");
    private static final Pattern REFERENCE_PATTERN = Pattern.compile("&([A-Za-z_][A-Za-z0-9_]*)(\\.)?");
    private static final int MAX_EXPANSION_PASSES = 8;

    private final Map<String, String> predefinedMacros;

    /**
     * Creates processor with optional predefined macros.
     *
     * @param predefinedMacros macros injected before file-local declarations.
     */
    public SasMacroProcessor(final Map<String, String> predefinedMacros) {
        this.predefinedMacros = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : predefinedMacros.entrySet()) {
            this.predefinedMacros.put(normalize(entry.getKey()), entry.getValue());
        }
    }

    /**
     * Expands macro declarations and references in content.
     *
     * @param source source SAS text.
     * @return expansion result containing output, effective macro table, and unresolved references.
     */
    public MacroExpansionResult expand(final String source) {
        final Map<String, String> macros = new LinkedHashMap<>(predefinedMacros);
        final Matcher macroDefMatcher = MACRO_DEF_PATTERN.matcher(source);
        final StringBuffer withoutMacroDefinitions = new StringBuffer();

        while (macroDefMatcher.find()) {
            final String name = normalize(macroDefMatcher.group(1));
            final String body = macroDefMatcher.group(2).trim();
            macros.put(name, body);
            macroDefMatcher.appendReplacement(withoutMacroDefinitions, "");
        }
        macroDefMatcher.appendTail(withoutMacroDefinitions);

        final Matcher letMatcher = LET_PATTERN.matcher(withoutMacroDefinitions.toString());
        final StringBuffer stripped = new StringBuffer();

        while (letMatcher.find()) {
            final String name = normalize(letMatcher.group(1));
            final String rawValue = letMatcher.group(2).trim();
            macros.put(name, rawValue);
            letMatcher.appendReplacement(stripped, "");
        }
        letMatcher.appendTail(stripped);

        String expanded = stripped.toString();
        final Set<String> unresolved = new LinkedHashSet<>();

        for (int pass = 0; pass < MAX_EXPANSION_PASSES; pass++) {
            final String macroExpanded = expandMacroCalls(expanded, macros, unresolved);
            final StringBuffer replaced = new StringBuffer();
            boolean changed = false;

            if (!macroExpanded.equals(expanded)) {
                expanded = macroExpanded;
                changed = true;
            }

            final Matcher variableMatcher = REFERENCE_PATTERN.matcher(expanded);

            while (variableMatcher.find()) {
                final String rawName = variableMatcher.group(1);
                final String normalized = normalize(rawName);
                final String replacement = macros.get(normalized);
                if (replacement == null) {
                    unresolved.add(rawName);
                    variableMatcher.appendReplacement(replaced, Matcher.quoteReplacement(variableMatcher.group(0)));
                    continue;
                }
                variableMatcher.appendReplacement(replaced, Matcher.quoteReplacement(replacement));
                changed = true;
            }
            variableMatcher.appendTail(replaced);
            expanded = replaced.toString();

            if (!changed) {
                break;
            }
        }

        return new MacroExpansionResult(expanded, macros, unresolved);
    }

    private String expandMacroCalls(
            final String source,
            final Map<String, String> macros,
            final Set<String> unresolved) {
        final Matcher callMatcher = MACRO_CALL_PATTERN.matcher(source);
        final StringBuffer replaced = new StringBuffer();

        while (callMatcher.find()) {
            final String rawName = callMatcher.group(1);
            final String normalized = normalize(rawName);

            if ("LET".equals(normalized) || "MACRO".equals(normalized) || "MEND".equals(normalized)) {
                callMatcher.appendReplacement(replaced, Matcher.quoteReplacement(callMatcher.group(0)));
                continue;
            }

            final String replacement = macros.get(normalized);
            if (replacement == null) {
                unresolved.add(rawName);
                callMatcher.appendReplacement(replaced, Matcher.quoteReplacement(callMatcher.group(0)));
                continue;
            }
            callMatcher.appendReplacement(replaced, Matcher.quoteReplacement(replacement));
        }
        callMatcher.appendTail(replaced);
        return replaced.toString();
    }

    private String normalize(final String name) {
        return name.toUpperCase(Locale.ROOT);
    }

    /**
     * Expansion output model used by {@link SasMacroGradleTask}.
     *
     * @param expandedSource expanded SAS source.
     * @param macros resolved macro table.
     * @param unresolvedMacros unresolved macro references encountered in text.
     */
    public record MacroExpansionResult(
            String expandedSource,
            Map<String, String> macros,
            Set<String> unresolvedMacros) {
    }
}

