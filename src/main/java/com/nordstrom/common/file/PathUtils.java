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
        
        PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + baseName + "*." + extension);
        
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
