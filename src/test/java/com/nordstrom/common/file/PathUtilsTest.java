package com.nordstrom.common.file;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

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
import org.testng.util.Strings;

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
            Files.createDirectories(targetPath);
        }

        Path path1 = PathUtils.getNextPath(targetPath, "testNextPath", "txt");
        assertEquals(path1.getFileName().toString(), "testNextPath.txt");

        path1.toFile().createNewFile();

        Path path2 = PathUtils.getNextPath(targetPath, "testNextPath", "txt");
        assertEquals(path2.getFileName().toString(), "testNextPath-1.txt");

        path2.toFile().createNewFile();
        targetPath.resolve("testNextPath-9.txt").toFile().createNewFile();
        targetPath.resolve("testNextPath-10.txt").toFile().createNewFile();

        Path path3 = PathUtils.getNextPath(targetPath, "testNextPath", "txt");
        assertEquals(path3.getFileName().toString(), "testNextPath-11.txt");

        Path path4 = PathUtils.getNextPath(targetPath, "test", "txt");
        assertEquals(path4.getFileName().toString(), "test.txt");
    }

    @Test
    public void testPrepend() {
        String[] actual = PathUtils.prepend("one", "two", "three");
        String[] expect = {"one", "two", "three"};
        assertEquals(actual, expect);
    }

    @Test
    public void testAppend() {
        String[] actual = PathUtils.append("three", "one", "two");
        String[] expect = {"one", "two", "three"};
        assertEquals(actual, expect);
    }

    @Test
    public void testBaseDir() {
        String actual = PathUtils.getBaseDir();
        String expect = getBasePath().toString();
        assertEquals(actual, expect);
    }

    @Test
    public void testPathForObject() {
        Path actual = PathUtils.ReportsDirectory.getPathForObject(this);
        Path expect = getBasePath().resolve("target").resolve("surefire-reports");
        assertEquals(actual, expect);
    }

    private Path getOutputPath() {
        ITestResult testResult = Reporter.getCurrentTestResult();
        ITestContext testContext = testResult.getTestContext();
        String outputDirectory = testContext.getOutputDirectory();
        return Paths.get(outputDirectory);
    }

    private Path getBasePath() {
        return Paths.get(System.getProperty("user.dir")).toAbsolutePath();
    }

    @Test(expectedExceptions = {AssertionError.class},
                    expectedExceptionsMessageRegExp = "PathUtils is a static utility class that cannot be instantiated")
    public void testPrivateConstructor() throws Throwable {

        Constructor<?>[] ctors;
        ctors = PathUtils.class.getDeclaredConstructors();
        assertEquals(ctors.length, 1, "PathUtils must have exactly one constructor");
        assertEquals(ctors[0].getModifiers() & Modifier.PRIVATE, Modifier.PRIVATE,
                        "PathUtils constructor must be private");
        assertEquals(ctors[0].getParameterTypes().length, 0, "PathUtils constructor must have no arguments");

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

    @Test(expectedExceptions = {NullPointerException.class})
    public void testNullBaseName() throws IOException {
        PathUtils.getNextPath(getOutputPath(), null, "txt");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void testEmptyBaseName() throws IOException {
        PathUtils.getNextPath(getOutputPath(), "", "txt");
    }

    @Test(expectedExceptions = {NullPointerException.class})
    public void testNullExtenstion() throws IOException {
        PathUtils.getNextPath(getOutputPath(), "test", null);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void testEmptyExtension() throws IOException {
        PathUtils.getNextPath(getOutputPath(), "test", "");
    }
    
    @Test
    public void testFindExecutableOnSystemPath() {
        String path = PathUtils.findExecutableOnSystemPath("java");
        assertNotNull(path);
    }

    @Test
    public void testFindExecutableByFullPath() {
    	String javaPath = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        String path = PathUtils.findExecutableOnSystemPath(javaPath);
        assertNotNull(path);
    }

    @Test
    public void testGetSystemPath() {
        String systemPath = PathUtils.getSystemPath();
        assertFalse(Strings.isNullOrEmpty(systemPath));
    }

}
