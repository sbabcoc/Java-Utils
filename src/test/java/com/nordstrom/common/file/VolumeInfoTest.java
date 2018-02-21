package com.nordstrom.common.file;

import java.io.IOException;
import java.util.Map;

import org.testng.annotations.Test;

import com.nordstrom.common.file.VolumeInfo.VolumeProps;

public class VolumeInfoTest {
    
    @Test
    public void test() throws IOException {
        Map<String, VolumeProps> propsList = VolumeInfo.getVolumeProps();
        for (VolumeProps thisProps : propsList.values()) {
            System.out.println("spec: " + thisProps.getSpec());
            System.out.println("file: " + thisProps.getFile());
            System.out.println("type: " + thisProps.getType());
            System.out.println("opts: " + String.join(",", thisProps.getOpts()));
            System.out.println("size: " + thisProps.getSize());
            System.out.println("free: " + thisProps.getFree());
            System.out.println("");
        }
    }

}
