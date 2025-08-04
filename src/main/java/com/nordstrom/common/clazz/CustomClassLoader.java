package com.nordstrom.common.clazz;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * This class implements a custom class loader initialized with a standard class path specification.
 */
public class CustomClassLoader extends ClassLoader {

    private final URL[] pathUrls;

    /**
     * Constructor for <b>CustomClassLoader</b> instances.
     * 
     * @param classpath class path for this class loader
     */
    public CustomClassLoader(String classpath) {
        pathUrls = Arrays.stream(classpath.split(File.pathSeparator)).map(entry -> {
            try {
                return Paths.get(entry).toUri().toURL();
            } catch (MalformedURLException e) {
                throw new UncheckedIOException("Invalid classpath entry: " + entry, e);
            }
        }).toArray(URL[]::new);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try (URLClassLoader classLoader = new URLClassLoader(pathUrls)) {
            return classLoader.loadClass(name);
        } catch (IOException e) {
            throw new ClassNotFoundException("Failed loading class: " + name, e);
        }
    }
}
