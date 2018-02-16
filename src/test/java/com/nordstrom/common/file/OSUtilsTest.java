package com.nordstrom.common.file;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

import com.nordstrom.common.file.OSUtils.OSProps;
import com.nordstrom.common.file.OSUtils.OSType;

public class OSUtilsTest {
    
    private static String osName = System.getProperty("os.name").toLowerCase();
    
    @Test
    public void testDefaultMapping() {
        OSUtils<OSType> osUtils = OSUtils.getDefault();
        OSType expected = (osName.startsWith("windows")) ? OSType.WINDOWS : OSType.UNIX;
        assertEquals(osUtils.getType(), expected, "Reported OS type doesn't match expected type");
    }
    
    @Test
    public void testCustomMapping() {
        OSUtils<TestEnum> osUtils = new OSUtils<>(TestEnum.class);
        assertEquals(osUtils.getType(), TestEnum.TEST, "Reported OS type doesn't match expected type");
    }
    
    @Test
    public void testAddOneMapping() {
        OSUtils<OSType> osUtils = OSUtils.getDefault();
        osUtils.put(TestEnum.TEST);
        assertEquals(osUtils.getType(), TestEnum.TEST, "Reported OS type doesn't match expected type");
    }
    
    @Test
    public void testAddMappingSet() {
        OSUtils<OSType> osUtils = OSUtils.getDefault();
        osUtils.putAll(TestEnum.class);
        assertEquals(osUtils.getType(), TestEnum.TEST, "Reported OS type doesn't match expected type");
    }
    
    @Test
    public void testOverrideMapping() {
        OSUtils<OSType> osUtils = OSUtils.getDefault();
        osUtils.put(OSType.SOLARIS, "(?i)" + osName);
        assertEquals(osUtils.getType(), OSType.SOLARIS, "Reported OS type doesn't match expected type");
    }
    
    @Test
    public void testUnsupportedType() {
        OSUtils<OSType> osUtils = OSUtils.getDefault();
        osUtils.put(osUtils.getType(), "");
        assertNull(osUtils.getType(), "Reported OS type doesn't match expected type");
    }
    
    public enum TestEnum implements OSProps {
        TEST;
        
        @Override
        public String pattern() {
            return "(?i)" + osName;
        }
        
    }
}
