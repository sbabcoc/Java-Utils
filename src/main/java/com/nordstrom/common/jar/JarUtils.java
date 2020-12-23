package com.nordstrom.common.jar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import com.google.common.base.Joiner;
import com.nordstrom.common.base.UncheckedThrow;

/**
 * This utility class provides methods related to Java JAR files:
 * 
 * <ul>
 *     <li>{@link #getClasspath} assemble a classpath string from the specified array of dependencies.</li>
 *     <li>{@link #findJarPathFor} find the path to the JAR file from which the named class was loaded.</li>
 *     <li>{@link #getJarPremainClass} gets the 'Premain-Class' attribute from the indicated JAR file.</li>
 * </ul>
 */
public class JarUtils {

    /**
     * Assemble a classpath string from the specified array of dependencies.
     * <p>
     * <b>NOTE</b>: If any of the specified dependency contexts names the {@code premain} class of a Java agent, the
     * string returned by this method will contain two records delimited by a {@code newline} character:
     * 
     * <ul>
     *     <li>0 - assembled classpath string</li>
     *     <li>1 - tab-delimited list of Java agent paths</li>
     * </ul>
     * 
     * @param dependencyContexts array of dependency contexts
     * @return assembled classpath string (see <b>NOTE</b>)
     */
    public static String getClasspath(final String[] dependencyContexts) {
        Set<String> agentList = new HashSet<>();
        Set<String> pathList = new HashSet<>();
        for (String contextClassName : dependencyContexts) {
            // get JAR path for this dependency context
            String jarPath = findJarPathFor(contextClassName);
            // if this context names the premain class of a Java agent
            if (contextClassName.equals(getJarPremainClass(jarPath))) {
                // collect agent path
                agentList.add(jarPath);
            // otherwise
            } else {
                // collect class path
                pathList.add(jarPath);
            }
        }
        // assemble classpath string
        String classPath = Joiner.on(File.pathSeparator).join(pathList);
        // if no agents were found
        if (agentList.isEmpty()) {
            // classpath only
            return classPath;
        } else {
            // classpath plus tab-delimited list of agent paths 
            return classPath + "\n" + Joiner.on("\t").join(agentList);
        }
    }

    /**
     * If the provided class has been loaded from a JAR file that is on the
     * local file system, will find the absolute path to that JAR file.
     * 
     * @param contextClassName
     *            The JAR file that contained the class file that represents
     *            this class will be found.
     * @return absolute path to the JAR file from which the specified class was
     *            loaded
     * @throws IllegalStateException
     *           If the specified class was loaded from a directory or in some
     *           other way (such as via HTTP, from a database, or some other
     *           custom class-loading device).
     */
    public static String findJarPathFor(final String contextClassName) {
        Class<?> contextClass;
        
        try {
            contextClass = Class.forName(contextClassName);
        } catch (ClassNotFoundException e) {
            throw UncheckedThrow.throwUnchecked(e);
        }
        
        String shortName = contextClassName;
        int idx = shortName.lastIndexOf('.');
        String protocol;
        
        if (idx > -1) {
            shortName = shortName.substring(idx + 1);
        }
        
        String uri = contextClass.getResource(shortName + ".class").toString();
        
        if (uri.startsWith("file:")) {
            protocol = "file:";
            String relPath = '/' + contextClassName.replace('.', '/') + ".class";
            if (uri.endsWith(relPath)) {
                idx = uri.length() - relPath.length();
            } else {
                throw new IllegalStateException(
                                "This class has been loaded from a class file, but I can't make sense of the path!");
            }
        } else if (uri.startsWith("jar:file:")) {
            protocol = "jar:file:";
            idx = uri.indexOf('!');
            if (idx == -1) {
                throw new IllegalStateException(
                                "You appear to have loaded this class from a local jar file, but I can't make sense of the URL!");
            }
        } else {
            idx = uri.indexOf(':');
            protocol = (idx > -1) ? uri.substring(0, idx) : "(unknown)";
            throw new IllegalStateException("This class has been loaded remotely via the " + protocol
                    + " protocol. Only loading from a jar on the local file system is supported.");
        }
        
        try {
            String fileName = URLDecoder.decode(uri.substring(protocol.length(), idx),
                            Charset.defaultCharset().name());
            return new File(fileName).getAbsolutePath();
        } catch (UnsupportedEncodingException e) {
            throw (InternalError) new InternalError(
                            "Default charset doesn't exist. Your VM is borked.").initCause(e);
        }
    }

    /**
     * Extract the 'Premain-Class' attribute from the manifest of the indicated JAR file.
     * 
     * @param jarPath absolute path to the JAR file
     * @return value of 'Premain-Class' attribute; {@code null} if unspecified
     */
    public static String  getJarPremainClass(String jarPath) {
        try(InputStream inputStream = new FileInputStream(jarPath);
            JarInputStream jarStream = new JarInputStream(inputStream)) {
            Manifest manifest = jarStream.getManifest();
            if (manifest != null) {
                return manifest.getMainAttributes().getValue("Premain-Class");
            }
        } catch (IOException e) {
            // nothing to do here
        }
        return null;
    }
    
}
