package com.nordstrom.common.jar;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import org.testng.annotations.Test;

public class JarUtilsTest {
    
    private static final String[] CONTEXTS = { "org.testng.annotations.Test", "com.beust.jcommander.JCommander",
            "org.apache.derby.jdbc.EmbeddedDriver", "com.google.common.base.Charsets" };
    
    @Test
    public void testClasspath() {
        String result = JarUtils.getClasspath(CONTEXTS);
        String[] paths = result.split(File.pathSeparator);
        assertEquals(paths.length, CONTEXTS.length, "path entry count mismatch");
        for (String thisPath : paths) {
            File file = new File(thisPath);
            assertTrue(file.exists(), "nonexistent path entry: " + thisPath);
        }
    }
    
}
