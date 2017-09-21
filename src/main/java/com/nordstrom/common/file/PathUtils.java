package com.nordstrom.common.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * This utility class provides a {@link #getNextPath(Path, String, String) getNextPath} method to acquire the next file
 * path in sequence for the specified base name and extension in the indicated target folder.  If the target folder
 * already contains at least one file that matches the specified base name and extension, the algorithm used to select
 * the next path will always return a path whose index is one more than the highest index that currently exists. (If a
 * single file with no index is found, its implied index is 1.)
 * <br><br>
 * <b>Example usage of {@code getNextPath}</b>
 * <pre>
 *     ...
 *     
 *     /*
 *      * This example gets the next path in sequence for base name `artifact`
 *      * and extension `txt` in the TestNG output directory.
 *      * 
 *      * For purposes of this example, the output directory already contains 
 *      * the following files: `artifact.txt`, `artifact-3.txt`
 *      *&#47;
 * 
 *     Path collectionPath = Paths.get(testContext.getOutputDirectory());
 *     // =&gt; C:\git\my-project\test-output\Default suite
 * 
 *     Path artifactPath;
 *     try {
 *         artifactPath = PathUtils.getNextPath(collectionPath, "artifact", "txt");
 *         // =&gt; C:\git\my-project\test-output\Default suite\artifact-4.txt
 *     } catch (IOException e) {
 *         provider.getLogger().info("Unable to get output path; no artifact was captured", e);
 *         return;
 *     }
 *     
 *     ...
 * </pre>
 */
public class PathUtils {

    private PathUtils() {
        throw new AssertionError("PathUtils is a static utility class that cannot be instantiated");
    }
    
    /**
     * Get the next available path in sequence for the specified base name and extension in the specified folder.
     * 
     * @param targetPath path to target directory for the next available path in sequence
     * @param baseName base name for the path sequence
     * @param extension extension for the path sequence
     * @return the next available path in sequence
     * @throws IOException if an I/O error is thrown when accessing the starting file.
     */
    public static Path getNextPath(Path targetPath, String baseName, String extension) throws IOException {
        String newName;
        
        Objects.requireNonNull(targetPath, "[targetPath] must be non-null");
        Objects.requireNonNull(baseName, "[baseName] must be non-null");
        Objects.requireNonNull(extension, "[extension] must be non-null");
        
        File targetFile = targetPath.toFile();
        if ( ! (targetFile.exists() && targetFile.isDirectory())) {
            throw new IllegalArgumentException("[targetPath] must specify an existing directory");
        }
        if (baseName.isEmpty()) {
            throw new IllegalArgumentException("[baseName] must specify a non-empty string");
        }
        if (extension.isEmpty()) {
            throw new IllegalArgumentException("[extension] must specify a non-empty string");
        }
        
        PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("regex:" + baseName + "(-\\d+)?\\." + extension);
        
        try (Stream<Path> stream = Files.walk(targetPath, 1)) {
            int ext = extension.length() + 1;
            
            Optional<String> optional =
                stream
                .map(Path::getFileName)
                .filter(pathMatcher::matches)
                .map(String::valueOf)
                .map(s -> s.substring(0, s.length() - ext))
                .sorted(Comparator.reverseOrder())
                .findFirst();
            
            if (optional.isPresent()) {
                int index = 1;
                Pattern pattern = Pattern.compile(baseName + "-(\\d+)");
                Matcher matcher = pattern.matcher(optional.get());
                if (matcher.matches()) {
                    index = Integer.parseInt(matcher.group(1));
                }
                newName = baseName + "-" + Integer.toString(index + 1) + "." + extension;
            } else {
                newName = baseName + "." + extension;
            }
        }
        
        return targetPath.resolve(newName);
    }
    
}
