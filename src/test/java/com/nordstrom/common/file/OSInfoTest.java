package com.nordstrom.common.file;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

import com.nordstrom.common.file.OSInfo.OSProps;
import com.nordstrom.common.file.OSInfo.OSType;

public class OSInfoTest {
    
    private static String osName = System.getProperty("os.name").toLowerCase();
    
    @Test
    public void testDefaultMapping() {
        OSInfo<OSType> osUtils = OSInfo.getDefault();
        OSType expected = (osName.startsWith("windows")) ? OSType.WINDOWS : OSType.UNIX;
        assertEquals(osUtils.getType(), expected, "Reported OS type doesn't match expected type");
    }
    
    @Test
    public void testCustomMapping() {
        OSInfo<TestEnum> osUtils = new OSInfo<>(TestEnum.class);
        assertEquals(osUtils.getType(), TestEnum.TEST, "Reported OS type doesn't match expected type");
    }
    
    @Test
    public void testAddOneMapping() {
        OSInfo<OSType> osUtils = OSInfo.getDefault();
        osUtils.put(TestEnum.TEST);
        assertEquals(osUtils.getType(), TestEnum.TEST, "Reported OS type doesn't match expected type");
    }
    
    @Test
    public void testAddMappingSet() {
        OSInfo<OSType> osUtils = OSInfo.getDefault();
        osUtils.putAll(TestEnum.class);
        assertEquals(osUtils.getType(), TestEnum.TEST, "Reported OS type doesn't match expected type");
    }
    
    @Test
    public void testOverrideMapping() {
        OSInfo<OSType> osUtils = OSInfo.getDefault();
        osUtils.put(OSType.SOLARIS, "(?i)" + osName);
        assertEquals(osUtils.getType(), OSType.SOLARIS, "Reported OS type doesn't match expected type");
    }
    
    @Test
    public void testUnsupportedType() {
        OSInfo<OSType> osUtils = OSInfo.getDefault();
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
