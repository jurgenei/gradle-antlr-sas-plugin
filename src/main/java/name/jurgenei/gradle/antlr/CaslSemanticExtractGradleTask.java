package name.jurgenei.gradle.antlr;

import name.jurgenei.gradle.antlr.semantic.NormalizedSemanticProgram;
import name.jurgenei.gradle.antlr.semantic.SemanticProgramJsonWriter;
import name.jurgenei.parsers.CaslParserBaseListener;
import name.jurgenei.parsers.CaslLexer;
import name.jurgenei.parsers.CaslParser;
import name.jurgenei.parsers.FedSqlLexer;
import name.jurgenei.parsers.FedSqlParser;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 * Extracts normalized semantic JSON from CASL XML AST files.
 */
@DisableCachingByDefault(because = "Semantic extraction reads generated XML AST files and emits derived JSON not yet fully declared for cache reproducibility")
public abstract class CaslSemanticExtractGradleTask extends DefaultTask {

    private final DirectoryProperty sourceDirectory;
    private final DirectoryProperty caslSourceDirectory;
    private final DirectoryProperty destinationDirectory;

    /**
     * Creates semantic extraction task defaults.
     *
     * @param objects Gradle object factory.
     */
    @Inject
    public CaslSemanticExtractGradleTask(final ObjectFactory objects) {
        this.sourceDirectory = objects.directoryProperty();
        this.caslSourceDirectory = objects.directoryProperty();
        this.destinationDirectory = objects.directoryProperty();

        sourceDirectory.convention(getProject().getLayout().getBuildDirectory().dir("casl/xmlast"));
        caslSourceDirectory.convention(getProject().getLayout().getProjectDirectory().dir("src/main/casl"));
        destinationDirectory.convention(getProject().getLayout().getBuildDirectory().dir("casl/semantic"));
    }

    /**
     * Source directory containing CASL XML AST files.
     *
     * @return source directory.
     */
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public DirectoryProperty getSourceDirectory() {
        return sourceDirectory;
    }

    /**
     * Source directory containing original CASL source files.
     *
     * @return CASL source directory.
     */
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public DirectoryProperty getCaslSourceDirectory() {
        return caslSourceDirectory;
    }

    /**
     * Destination directory receiving semantic JSON files.
     *
     * @return destination directory.
     */
    @OutputDirectory
    public DirectoryProperty getDestinationDirectory() {
        return destinationDirectory;
    }

    /**
     * Extracts semantic JSON per XML file while preserving relative paths.
     */
    @TaskAction
    public void extract() {
        final File sourceDir = sourceDirectory.get().getAsFile();
        if (!sourceDir.isDirectory()) {
            throw new GradleException("sourceDirectory must be an existing directory: " + sourceDir);
        }
        final File caslSourceDir = caslSourceDirectory.get().getAsFile();
        if (!caslSourceDir.isDirectory()) {
            throw new GradleException("caslSourceDirectory must be an existing directory: " + caslSourceDir);
        }

        final Path sourceRoot = sourceDir.toPath();
        final Path caslSourceRoot = caslSourceDir.toPath();
        final Path destinationRoot = destinationDirectory.get().getAsFile().toPath();
        final SemanticProgramJsonWriter writer = new SemanticProgramJsonWriter();

        final List<Path> xmlFiles;
        try (Stream<Path> stream = Files.walk(sourceRoot)) {
            xmlFiles = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".xml"))
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            throw new GradleException("Failed scanning AST directory " + sourceRoot, ex);
        }

        for (Path xmlFile : xmlFiles) {
            final Path relative = sourceRoot.relativize(xmlFile);
            final String sourceContent;
            try {
                sourceContent = Files.readString(resolveCaslSource(caslSourceRoot, relative), StandardCharsets.UTF_8);
            } catch (IOException ex) {
                throw new GradleException("Failed reading CASL source for AST file " + xmlFile, ex);
            }

            final SemanticExtractionResult extraction = extractSemantics(sourceContent, relative);
            final NormalizedSemanticProgram program = new NormalizedSemanticProgram(
                    "casl",
                    relative.toString(),
                    extraction.actions(),
                    extraction.fedSqlQueryCount(),
                    extraction.fedSqlParsedCount(),
                    extraction.fedSqlFailedCount(),
                    extraction.fedSqlErrors());
            final String json = writer.write(program);

            final String jsonFileName = toJsonName(relative.getFileName().toString());
            final Path jsonRelative = relative.getParent() == null ? Path.of(jsonFileName) : relative.getParent().resolve(jsonFileName);
            final Path target = destinationRoot.resolve(jsonRelative);
            try {
                Files.createDirectories(target.getParent());
                Files.writeString(target, json, StandardCharsets.UTF_8);
            } catch (IOException ex) {
                throw new GradleException("Failed writing semantic file " + target, ex);
            }

            getLogger().lifecycle("[CASL-SEMANTIC] {}", jsonRelative.toString().replace(File.separatorChar, '/'));
        }
    }

    private SemanticExtractionResult extractSemantics(final String sourceContent, final Path relativePath) {
        final CaslLexer lexer = new CaslLexer(CharStreams.fromString(sourceContent));
        final CollectingErrorListener caslErrors = new CollectingErrorListener();
        lexer.removeErrorListeners();
        lexer.addErrorListener(caslErrors);

        final CaslParser parser = new CaslParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(caslErrors);

        final CaslParser.ProgramContext program = parser.program();
        if (!caslErrors.messages().isEmpty()) {
            throw new GradleException("Failed parsing CASL source " + relativePath + ": " + String.join(" | ", caslErrors.messages()));
        }

        final SemanticCollector collector = new SemanticCollector(relativePath.toString());
        ParseTreeWalker.DEFAULT.walk(collector, program);
        return collector.toResult();
    }

    private String normalizeAction(final String moduleName, final String actionName) {
        final String key = moduleName.toLowerCase(Locale.ROOT) + "." + actionName.toLowerCase(Locale.ROOT);
        if ("table.loadtable".equals(key)) {
            return "table.loadTable";
        }
        if ("fedsql.execdirect".equals(key)) {
            return "fedSql.execDirect";
        }
        return moduleName + "." + actionName;
    }

    private FedSqlParseResult parseFedSqlQuery(final String queryText) {
        final FedSqlLexer lexer = new FedSqlLexer(CharStreams.fromString(queryText));
        final CollectingErrorListener fedSqlErrors = new CollectingErrorListener();
        lexer.removeErrorListeners();
        lexer.addErrorListener(fedSqlErrors);

        final FedSqlParser parser = new FedSqlParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(fedSqlErrors);
        final FedSqlParser.QueryContext query = parser.query();

        if (!fedSqlErrors.messages().isEmpty()) {
            return new FedSqlParseResult(false, String.join(" | ", fedSqlErrors.messages()));
        }
        if (query == null) {
            return new FedSqlParseResult(false, "FEDSQL parser returned empty query context");
        }
        return new FedSqlParseResult(true, null);
    }

    private String decodeStringLiteral(final String literal) {
        final String trimmed = literal.trim();
        if (trimmed.length() < 2 || !trimmed.startsWith("'") || !trimmed.endsWith("'")) {
            return trimmed;
        }
        final String raw = trimmed.substring(1, trimmed.length() - 1);
        return raw.replace("''", "'");
    }

    private String toJsonName(final String fileName) {
        final int dot = fileName.lastIndexOf('.');
        final String base = dot > 0 ? fileName.substring(0, dot) : fileName;
        return base + ".semantic.json";
    }

    private final class SemanticCollector extends CaslParserBaseListener {

        private final String sourceLabel;
        private final List<String> actions = new ArrayList<>();
        private final List<String> fedSqlErrors = new ArrayList<>();
        private int fedSqlQueryCount;
        private int fedSqlParsedCount;
        private int fedSqlFailedCount;

        private SemanticCollector(final String sourceLabel) {
            this.sourceLabel = sourceLabel;
        }

        @Override
        public void enterActionRunStatement(final CaslParser.ActionRunStatementContext ctx) {
            if (ctx.identifier().size() < 2) {
                return;
            }
            final String moduleName = ctx.identifier(0).getText();
            final String actionName = ctx.identifier(1).getText();
            final String normalizedAction = normalizeAction(moduleName, actionName);
            actions.add(normalizedAction);

            if (!"fedSql.execDirect".equals(normalizedAction)) {
                return;
            }

            fedSqlQueryCount++;
            final String queryPayload = resolveFedSqlQueryPayload(ctx);
            if (queryPayload == null) {
                fedSqlFailedCount++;
                fedSqlErrors.add(sourceLabel + ": missing query= argument for fedSql.execDirect");
                return;
            }

            final FedSqlParseResult parseResult = parseFedSqlQuery(queryPayload);
            if (parseResult.ok()) {
                fedSqlParsedCount++;
            } else {
                fedSqlFailedCount++;
                fedSqlErrors.add(sourceLabel + ": " + parseResult.error());
            }
        }

        private String resolveFedSqlQueryPayload(final CaslParser.ActionRunStatementContext ctx) {
            if (ctx.argumentList() == null) {
                return null;
            }
            for (CaslParser.ArgumentContext argument : ctx.argumentList().argument()) {
                final String name = argument.identifier().getText();
                if (!"query".equalsIgnoreCase(name)) {
                    continue;
                }
                return decodeStringLiteral(argument.expression().getText());
            }
            return null;
        }

        private SemanticExtractionResult toResult() {
            return new SemanticExtractionResult(actions, fedSqlQueryCount, fedSqlParsedCount, fedSqlFailedCount, fedSqlErrors);
        }
    }

    private record SemanticExtractionResult(
            List<String> actions,
            int fedSqlQueryCount,
            int fedSqlParsedCount,
            int fedSqlFailedCount,
            List<String> fedSqlErrors) {
    }

    private record FedSqlParseResult(boolean ok, String error) {
    }

    private static final class CollectingErrorListener extends BaseErrorListener {

        private final List<String> messages = new ArrayList<>();

        @Override
        public void syntaxError(
                final Recognizer<?, ?> recognizer,
                final Object offendingSymbol,
                final int line,
                final int charPositionInLine,
                final String msg,
                final RecognitionException e) {
            messages.add(line + ":" + charPositionInLine + " " + msg);
        }

        private List<String> messages() {
            return messages;
        }
    }

    private Path resolveCaslSource(final Path caslSourceRoot, final Path xmlRelativePath) {
        final String fileName = xmlRelativePath.getFileName().toString();
        final int dot = fileName.lastIndexOf('.');
        final String base = dot > 0 ? fileName.substring(0, dot) : fileName;
        final Path parent = xmlRelativePath.getParent();

        final Path casl = parent == null
                ? caslSourceRoot.resolve(base + ".casl")
                : caslSourceRoot.resolve(parent).resolve(base + ".casl");
        if (Files.exists(casl)) {
            return casl;
        }

        final Path cas = parent == null
                ? caslSourceRoot.resolve(base + ".cas")
                : caslSourceRoot.resolve(parent).resolve(base + ".cas");
        if (Files.exists(cas)) {
            return cas;
        }

        throw new GradleException("No CASL source file found for AST path " + xmlRelativePath);
    }
}


