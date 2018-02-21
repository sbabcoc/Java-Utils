package com.nordstrom.common.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nordstrom.common.file.OSInfo.OSType;

public class VolumeInfo {
    
    static final boolean IS_WINDOWS = (OSInfo.getDefault().getType() == OSType.WINDOWS);
    
    private VolumeInfo() {
        throw new AssertionError("VolumeInfo is a static utility class that cannot be instantiated");
    }
    
    public static List<VolumeProps> getVolumeProps() throws IOException {
        List<VolumeProps> propsList = new ArrayList<>();
        Pattern template = Pattern.compile("(.+) on (.+) type (.+) \\((.+)\\)");
        
        Process mountProcess;
        if (IS_WINDOWS) {
            String[] cmd = {"sh", "-c", "mount | grep noumount"};
            mountProcess = Runtime.getRuntime().exec(cmd);
        } else {
            mountProcess = Runtime.getRuntime().exec("mount");
        }
        
        InputStreamReader isr = new InputStreamReader(mountProcess.getInputStream());
        try(BufferedReader mountOutput = new BufferedReader(isr)) {
            String line;
            while(null != (line = mountOutput.readLine())) {
                Matcher matcher = template.matcher(line);
                if (matcher.matches()) {
                    String spec = matcher.group(1);
                    String file = matcher.group(2);
                    String type = matcher.group(3);
                    String[] opts = matcher.group(4).split(",");
                    propsList.add(new VolumeProps(spec, file, type, opts));
                }
            }
        }
        return propsList;
    }
    
    public static class VolumeProps {
        
        String spec; // block device or remote file system
        String file; // file system mount point
        String type; // file system type
        String[] opts; // file system mount options
        
        private long size;
        private long free;
        
        VolumeProps(String spec, String file, String type, String... opts) {
            this.spec = spec;
            this.file = file;
            this.type = type;
            this.opts = opts;
            
            File f;
            if (IS_WINDOWS) {
                f = new File(spec);
            } else {
                f = new File(file);
            }
            
            this.size = f.getTotalSpace();
            this.free = f.getFreeSpace();
        }
        
        public String getSpec() {
            return spec;
        }

        public String getFile() {
            return file;
        }

        public String getType() {
            return type;
        }

        public String[] getOpts() {
            return opts;
        }
        
        public long getSize() {
            return size;
        }
        public long getFree() {
            return free;
        }
    }
}
