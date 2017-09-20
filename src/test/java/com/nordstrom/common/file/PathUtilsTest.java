package com.nordstrom.common.file;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.Test;

public class PathUtilsTest {
    
    @Test
    public void testNextPath() throws IOException {
        Path outputDir = getOutputPath();
        Path targetPath = outputDir.resolve("targetPath");
        if (targetPath.toFile().exists()) {
            for (File file : targetPath.toFile().listFiles()) {
                file.delete();
            }
        } else {
            Files.createDirectory(targetPath);
        }
        
        Path path1 = PathUtils.getNextPath(targetPath, "test", "txt");
        assertEquals(path1.getFileName().toString(), "test.txt");
        
        path1.toFile().createNewFile();
        Path path2 = PathUtils.getNextPath(targetPath, "test", "txt");
        assertEquals(path2.getFileName().toString(), "test-2.txt");
    }

    private Path getOutputPath() {
        ITestResult testResult = Reporter.getCurrentTestResult();
        ITestContext testContext = testResult.getTestContext();
        String outputDirectory = testContext.getOutputDirectory();
        Path outputDir = Paths.get(outputDirectory);
        return outputDir;
    }

    @Test(expectedExceptions = {AssertionError.class},
                    expectedExceptionsMessageRegExp = "PathUtils is a static utility class that cannot be instantiated")
    public void testPrivateConstructor() throws Throwable {
        
        Constructor<?>[] ctors;
        ctors = PathUtils.class.getDeclaredConstructors();
        assertEquals(ctors.length, 1, "GridUtility must have exactly one constructor");
        assertEquals(ctors[0].getModifiers() & Modifier.PRIVATE, Modifier.PRIVATE,
                        "GridUtility constructor must be private");
        assertEquals(ctors[0].getParameterTypes().length, 0, "GridUtility constructor must have no arguments");
        
        try {
            ctors[0].setAccessible(true);
            ctors[0].newInstance();
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
    
    @Test(expectedExceptions = {NullPointerException.class})
    public void testNullPath() throws IOException {
        PathUtils.getNextPath(null, "test", "txt");
    }
    
    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void testNonExistentPath() throws IOException {
        PathUtils.getNextPath(Paths.get("foobar"), "test", "txt");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void testNullBaseName() throws IOException {
        PathUtils.getNextPath(getOutputPath(), null, "txt");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void testEmptyBaseName() throws IOException {
        PathUtils.getNextPath(getOutputPath(), "", "txt");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void testNullExtenstion() throws IOException {
        PathUtils.getNextPath(getOutputPath(), "test", null);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void testEmptyExtension() throws IOException {
        PathUtils.getNextPath(getOutputPath(), "test", "");
    }
}
