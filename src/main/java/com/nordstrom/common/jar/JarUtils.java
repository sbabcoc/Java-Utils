package com.nordstrom.common.jar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
 * 
 * <b>NOTE</b>: The core implementation for the {@link #findJarPathFor(String)} method was shared on
 * <a href="https://stackoverflow.com">Stack Overflow</a> by <a href="https://stackoverflow.com/users/125844/notnoop">
 * notnoop</a> in their <a href="https://stackoverflow.com/a/1983875">answer</a> to a question regarding
 * how to locate the JAR file from which a specified class was loaded. This code was attributed to the
 * <a href="https://github.com/rzwitserloot/lombok.patcher">lombok.patcher</a> project, published by
 * <a href="https://github.com/rzwitserloot">Reinier Zwitserloot</a>. An updated version of the original
 * code can be found <a 
 * href="https://github.com/rzwitserloot/lombok.patcher/blob/master/src/patcher/lombok/patcher/ClassRootFinder.java">
 * here</a>.
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
        // get dependency context paths
        List<String> contextPaths = getContextPaths(false, dependencyContexts);
        // pop classpath from collection
        String classPath = contextPaths.remove(0);
        // if no agents were found
        if (contextPaths.isEmpty()) {
            // classpath only
            return classPath;
        } else {
            // classpath plus tab-delimited list of agent paths 
            return classPath + "\n" + Joiner.on("\t").join(contextPaths);
        }
    }
    
    /**
     * Assemble a list of context paths from the specified array of dependencies.
     * <p>
     * <b>NOTE</b>: The first item of the returned list contains a path-delimited string of JAR file paths suitable
     * for use with the Java {@code -cp} command line option. If any of the specified dependency contexts names the
     * {@code premain} class of a Java agent, subsequent items contain fully-formed {@code -javaagent} specifications.
     * 
     * @param dependencyContexts array of dependency contexts
     * @return list of classpath/javaagent specifications (see <b>NOTE</b>)
     */
    public static List<String> getContextPaths(final String[] dependencyContexts) {
        return getContextPaths(true, dependencyContexts);
    }
    
    /**
     * Assemble a list of context paths from the specified array of dependencies.
     * <p>
     * <b>NOTE</b>: The first item of the returned list contains a path-delimited string of JAR file paths suitable
     * for use with the Java {@code -cp} command line option. If any of the specified dependency contexts names the
     * {@code premain} class of a Java agent, subsequent items contain Java agent JAR paths with optional prefix.
     * 
     * @param prefixAgents {@code true} to request prefixing Java agent paths with {@code -javaagent:}
     * @param dependencyContexts array of dependency contexts
     * @return list of classpath/javaagent paths (see <b>NOTE</b>)
     */
    private static List<String> getContextPaths(final boolean prefixAgents, final String[] dependencyContexts) {
        Set<String> pathList = new HashSet<>();
        Set<String> agentList = new HashSet<>();
        List<String> contextPaths = new ArrayList<>();
        final String prefix = prefixAgents ? "-javaagent:" : "";
        for (String contextClassName : dependencyContexts) {
            // get JAR path for this dependency context
            String jarPath = findJarPathFor(contextClassName);
            // if this context names the premain class of a Java agent
            if (contextClassName.equals(getJarPremainClass(jarPath))) {
                // collect agent path
                agentList.add(prefix + jarPath);
            // otherwise
            } else {
                // collect class path
                pathList.add(jarPath);
            }
        }
        // add assembled classpath string
        contextPaths.add(Joiner.on(File.pathSeparator).join(pathList));
        // add Java agent paths
        contextPaths.addAll(agentList);
        return contextPaths;
    }

    /**
     * If the provided class has been loaded from a JAR file that is on the
     * local file system, will find the absolute path to that JAR file.
     * <p>
     * <b>NOTE</b>: The core implementation of this method was lifted from
     * the {@code lombok.patcher} project. See the comments at the head of
     * {@link JarUtils this class} for details.
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
                throw new IllegalStateException("Unrecognized structure file-protocol path: " + uri);
            }
        } else if (uri.startsWith("jar:file:")) {
            protocol = "jar:file:";
            idx = uri.indexOf('!');
            if (idx == -1) {
                throw new IllegalStateException("No separator found in jar-protocol path: " + uri);
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
