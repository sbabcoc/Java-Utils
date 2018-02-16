package com.nordstrom.common.file;

import org.testng.annotations.Test;

import com.nordstrom.common.file.OSUtils.OSType;

public class OSUtilsTest {
    
    @Test
    public void testDefault() {
        OSUtils<OSType> osUtils = OSUtils.getDefault();
        System.out.println(osUtils.getType());
        System.out.println(OSUtils.osName());
        System.out.println(OSUtils.version());
        System.out.println(OSUtils.arch());
    }

}
