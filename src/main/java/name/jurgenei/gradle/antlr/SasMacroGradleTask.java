package name.jurgenei.gradle.antlr;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
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
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Expands SAS macros from source files into normalized intermediate SAS files.
 *
 * <p>This task is first stage in chained workflow:</p>
 * <pre>
 * SasProgramFile -> SasMacroGradleTask -> XmlAstSasGradleTask -> XML AST
 * </pre>
 */
@DisableCachingByDefault(because = "Macro expansion is file-system driven and this proof-of-concept does not yet declare full incremental cache semantics")
public abstract class SasMacroGradleTask extends DefaultTask {

    private final DirectoryProperty sourceDirectory;
    private final DirectoryProperty destinationDirectory;
    private final ListProperty<String> includes;
    private final ListProperty<String> excludes;
    private final MapProperty<String, String> predefinedMacros;
    private final Property<Boolean> failOnUndefinedMacro;

    /**
     * Creates macro task with SAS defaults.
     *
     * @param objects Gradle object factory.
     */
    @Inject
    public SasMacroGradleTask(final ObjectFactory objects) {
        this.sourceDirectory = objects.directoryProperty();
        this.destinationDirectory = objects.directoryProperty();
        this.includes = objects.listProperty(String.class);
        this.excludes = objects.listProperty(String.class);
        this.predefinedMacros = objects.mapProperty(String.class, String.class);
        this.failOnUndefinedMacro = objects.property(Boolean.class);

        sourceDirectory.convention(getProject().getLayout().getProjectDirectory().dir("src/main/sas"));
        destinationDirectory.convention(getProject().getLayout().getBuildDirectory().dir("sas/macro"));
        includes.convention(List.of("**/*.sas"));
        excludes.convention(List.of());
        predefinedMacros.convention(java.util.Map.of());
        failOnUndefinedMacro.convention(false);
    }

    /**
     * Source folder scanned for SAS files.
     *
     * @return source directory.
     */
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public DirectoryProperty getSourceDirectory() {
        return sourceDirectory;
    }

    /**
     * Destination folder containing expanded SAS files.
     *
     * @return destination directory.
     */
    @OutputDirectory
    public DirectoryProperty getDestinationDirectory() {
        return destinationDirectory;
    }

    /**
     * Ant-style include patterns relative to {@link #getSourceDirectory()}.
     *
     * @return include patterns.
     */
    @Input
    public ListProperty<String> getIncludes() {
        return includes;
    }

    /**
     * Ant-style exclude patterns relative to {@link #getSourceDirectory()}.
     *
     * @return exclude patterns.
     */
    @Input
    public ListProperty<String> getExcludes() {
        return excludes;
    }

    /**
     * Macro values provided by build logic before file-local declarations.
     *
     * @return macro map.
     */
    @Input
    public MapProperty<String, String> getPredefinedMacros() {
        return predefinedMacros;
    }

    /**
     * Fails task when unresolved {@code &macro} references remain after expansion.
     *
     * @return fail-on-undefined flag.
     */
    @Input
    public Property<Boolean> getFailOnUndefinedMacro() {
        return failOnUndefinedMacro;
    }

    /**
     * Expands all selected SAS files into destination directory preserving relative paths.
     */
    @TaskAction
    public void expand() {
        final File sourceDir = sourceDirectory.get().getAsFile();
        if (!sourceDir.isDirectory()) {
            throw new GradleException("sourceDirectory must be an existing directory: " + sourceDir);
        }

        final Path sourceRoot = sourceDir.toPath();
        final File destinationDir = destinationDirectory.get().getAsFile();
        destinationDir.mkdirs();

        final List<PathMatcher> includeMatchers = toMatchers(includes.get());
        final List<PathMatcher> excludeMatchers = toMatchers(excludes.get());
        final List<File> files = selectFiles(sourceRoot, includeMatchers, excludeMatchers);

        final SasMacroProcessor processor = new SasMacroProcessor(predefinedMacros.get());
        final Set<String> unresolved = new LinkedHashSet<>();

        for (File sourceFile : files) {
            final Path sourcePath = sourceFile.toPath();
            final Path relative = sourceRoot.relativize(sourcePath);
            final Path targetPath = destinationDir.toPath().resolve(relative);
            writeExpandedFile(processor, sourcePath, targetPath, unresolved);
            getLogger().lifecycle("[SAS-MACRO] {}", relative.toString().replace(File.separatorChar, '/'));
        }

        if (!unresolved.isEmpty() && failOnUndefinedMacro.get()) {
            throw new GradleException("Undefined macro references detected: " + String.join(", ", unresolved));
        }
        if (!unresolved.isEmpty()) {
            getLogger().warn("Undefined macro references detected: {}", unresolved);
        }
    }

    private void writeExpandedFile(
            final SasMacroProcessor processor,
            final Path sourcePath,
            final Path targetPath,
            final Set<String> unresolved) {
        try {
            final String source = Files.readString(sourcePath, StandardCharsets.UTF_8);
            final SasMacroProcessor.MacroExpansionResult result = processor.expand(source);
            unresolved.addAll(result.unresolvedMacros());
            Files.createDirectories(targetPath.getParent());
            Files.writeString(targetPath, result.expandedSource(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new GradleException("Failed macro expansion for file " + sourcePath, ex);
        }
    }

    private List<File> selectFiles(
            final Path sourceRoot,
            final List<PathMatcher> includeMatchers,
            final List<PathMatcher> excludeMatchers) {
        try (Stream<Path> stream = Files.walk(sourceRoot)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(sourceRoot::relativize)
                    .filter(path -> matchesAny(path, includeMatchers))
                    .filter(path -> !matchesAny(path, excludeMatchers))
                    .map(sourceRoot::resolve)
                    .map(Path::toFile)
                    .sorted(Comparator.comparing(File::getAbsolutePath))
                    .collect(Collectors.toCollection(ArrayList::new));
        } catch (IOException ex) {
            throw new GradleException("Failed scanning source directory " + sourceRoot, ex);
        }
    }

    private List<PathMatcher> toMatchers(final List<String> patterns) {
        final List<PathMatcher> matchers = new ArrayList<>();
        for (String pattern : patterns) {
            matchers.add(FileSystems.getDefault().getPathMatcher("glob:" + pattern));
            if (pattern.startsWith("**/")) {
                matchers.add(FileSystems.getDefault().getPathMatcher("glob:" + pattern.substring(3)));
            }
        }
        return matchers;
    }

    private boolean matchesAny(final Path relativePath, final List<PathMatcher> matchers) {
        if (matchers.isEmpty()) {
            return false;
        }
        for (PathMatcher matcher : matchers) {
            if (matcher.matches(relativePath)) {
                return true;
            }
        }
        return false;
    }
}

