package name.jurgenei.gradle.antlr;

import name.jurgenei.gradle.antlr.semantic.Ds2SemanticProgram;
import name.jurgenei.gradle.antlr.semantic.Ds2SemanticProgramJsonWriter;
import name.jurgenei.parsers.Ds2BaseListener;
import name.jurgenei.parsers.Ds2Lexer;
import name.jurgenei.parsers.Ds2Parser;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
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

/**
 * Extracts DS2 semantic JSON summaries from DS2 source files.
 */
@DisableCachingByDefault(because = "Semantic extraction reads DS2 source files and emits derived JSON not yet fully declared for cache reproducibility")
public abstract class Ds2SemanticExtractGradleTask extends DefaultTask {

    private final DirectoryProperty sourceDirectory;
    private final DirectoryProperty destinationDirectory;

    /**
     * Creates semantic extraction task defaults.
     *
     * @param objects Gradle object factory.
     */
    @Inject
    public Ds2SemanticExtractGradleTask(final ObjectFactory objects) {
        this.sourceDirectory = objects.directoryProperty();
        this.destinationDirectory = objects.directoryProperty();

        sourceDirectory.convention(getProject().getLayout().getProjectDirectory().dir("src/main/ds2"));
        destinationDirectory.convention(getProject().getLayout().getBuildDirectory().dir("ds2/semantic"));
    }

    /**
     * Source directory containing DS2 files.
     *
     * @return source directory.
     */
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public DirectoryProperty getSourceDirectory() {
        return sourceDirectory;
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
     * Extracts semantic JSON per DS2 file while preserving relative paths.
     */
    @TaskAction
    public void extract() {
        final File sourceDir = sourceDirectory.get().getAsFile();
        if (!sourceDir.isDirectory()) {
            throw new GradleException("sourceDirectory must be an existing directory: " + sourceDir);
        }

        final Path sourceRoot = sourceDir.toPath();
        final Path destinationRoot = destinationDirectory.get().getAsFile().toPath();
        final Ds2SemanticProgramJsonWriter writer = new Ds2SemanticProgramJsonWriter();

        final List<Path> sourceFiles;
        try (Stream<Path> stream = Files.walk(sourceRoot)) {
            sourceFiles = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        final String name = path.getFileName().toString();
                        return name.endsWith(".ds2") || name.endsWith(".sas");
                    })
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            throw new GradleException("Failed scanning DS2 source directory " + sourceRoot, ex);
        }

        for (Path sourceFile : sourceFiles) {
            final Path relative = sourceRoot.relativize(sourceFile);
            final String sourceText;
            try {
                sourceText = Files.readString(sourceFile, StandardCharsets.UTF_8);
            } catch (IOException ex) {
                throw new GradleException("Failed reading DS2 source " + sourceFile, ex);
            }

            final Ds2ExtractionResult extraction = extractSemantics(sourceText, relative);
            final Ds2SemanticProgram program = new Ds2SemanticProgram(
                    "ds2",
                    relative.toString(),
                    extraction.methodNames(),
                    extraction.declarationNames(),
                    extraction.methodCallEdges(),
                    extraction.procDs2BlockCount(),
                    extraction.dataBlockNames(),
                    extraction.packageBlockNames(),
                    extraction.threadBlockNames(),
                    extraction.setFromSources());
            final String json = writer.write(program);

            final String jsonFileName = toJsonName(relative.getFileName().toString());
            final Path jsonRelative = relative.getParent() == null ? Path.of(jsonFileName) : relative.getParent().resolve(jsonFileName);
            final Path target = destinationRoot.resolve(jsonRelative);
            try {
                Files.createDirectories(target.getParent());
                Files.writeString(target, json, StandardCharsets.UTF_8);
            } catch (IOException ex) {
                throw new GradleException("Failed writing DS2 semantic file " + target, ex);
            }

            getLogger().lifecycle("[DS2-SEMANTIC] {}", jsonRelative.toString().replace(File.separatorChar, '/'));
        }
    }

    private Ds2ExtractionResult extractSemantics(final String sourceText, final Path relativePath) {
        final Ds2Lexer lexer = new Ds2Lexer(CharStreams.fromString(sourceText));
        final CollectingErrorListener errors = new CollectingErrorListener();
        lexer.removeErrorListeners();
        lexer.addErrorListener(errors);

        final Ds2Parser parser = new Ds2Parser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(errors);

        final Ds2Parser.ProgramContext program = parser.program();
        if (!errors.messages().isEmpty()) {
            throw new GradleException("Failed parsing DS2 source " + relativePath + ": " + String.join(" | ", errors.messages()));
        }

        final SemanticCollector collector = new SemanticCollector();
        ParseTreeWalker.DEFAULT.walk(collector, program);
        return collector.toResult();
    }

    private String toJsonName(final String fileName) {
        final int dot = fileName.lastIndexOf('.');
        final String base = dot > 0 ? fileName.substring(0, dot) : fileName;
        return base + ".semantic.json";
    }

    private static final class SemanticCollector extends Ds2BaseListener {

        private final List<String> methodNames = new ArrayList<>();
        private final List<String> declarationNames = new ArrayList<>();
        private final List<String> methodCallEdges = new ArrayList<>();
        private final List<String> dataBlockNames = new ArrayList<>();
        private final List<String> packageBlockNames = new ArrayList<>();
        private final List<String> threadBlockNames = new ArrayList<>();
        private final List<String> setFromSources = new ArrayList<>();
        private int procDs2BlockCount;
        private String currentMethod = "<global>";

        @Override
        public void enterProcDs2Block(final Ds2Parser.ProcDs2BlockContext ctx) {
            procDs2BlockCount++;
        }

        @Override
        public void enterDataBlock(final Ds2Parser.DataBlockContext ctx) {
            dataBlockNames.add(ctx.identifier().getText());
        }

        @Override
        public void enterPackageBlock(final Ds2Parser.PackageBlockContext ctx) {
            packageBlockNames.add(ctx.packageName().getText());
        }

        @Override
        public void enterThreadBlock(final Ds2Parser.ThreadBlockContext ctx) {
            threadBlockNames.add(ctx.identifier().getText());
        }

        @Override
        public void enterMethodDecl(final Ds2Parser.MethodDeclContext ctx) {
            final String methodName = ctx.methodName().getText();
            methodNames.add(methodName);
            currentMethod = methodName;
        }

        @Override
        public void exitMethodDecl(final Ds2Parser.MethodDeclContext ctx) {
            currentMethod = "<global>";
        }

        @Override
        public void enterDeclarationStatement(final Ds2Parser.DeclarationStatementContext ctx) {
            if (ctx.declarationType() != null && !ctx.declarationItem().isEmpty()) {
                declarationNames.add(ctx.declarationType().getText() + " " + ctx.declarationItem(0).identifier().getText());
            }
        }

        @Override
        public void enterSetFromStatement(final Ds2Parser.SetFromStatementContext ctx) {
            setFromSources.add(ctx.identifier().getText());
        }

        @Override
        public void enterMethodCallStatement(final Ds2Parser.MethodCallStatementContext ctx) {
            final List<Ds2Parser.IdentifierContext> identifiers = ctx.identifier();
            if (identifiers.isEmpty()) {
                return;
            }
            final String callee;
            if (identifiers.size() == 1) {
                callee = identifiers.get(0).getText();
            } else {
                callee = identifiers.get(identifiers.size() - 2).getText() + "." + identifiers.get(identifiers.size() - 1).getText();
            }
            methodCallEdges.add(currentMethod + "->" + normalizeEdgeTarget(callee));
        }

        private String normalizeEdgeTarget(final String callee) {
            final String key = callee.toLowerCase(Locale.ROOT);
            if ("this.run".equals(key)) {
                return "run";
            }
            return callee;
        }

        private Ds2ExtractionResult toResult() {
            return new Ds2ExtractionResult(
                    methodNames,
                    declarationNames,
                    methodCallEdges,
                    procDs2BlockCount,
                    dataBlockNames,
                    packageBlockNames,
                    threadBlockNames,
                    setFromSources);
        }
    }

    private record Ds2ExtractionResult(
            List<String> methodNames,
            List<String> declarationNames,
            List<String> methodCallEdges,
            int procDs2BlockCount,
            List<String> dataBlockNames,
            List<String> packageBlockNames,
            List<String> threadBlockNames,
            List<String> setFromSources) {
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
}


