package com.nordstrom.common.uri;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class UriUtils {

    private UriUtils() {
        throw new AssertionError("UriUtils is a static utility class that cannot be instantiated.");
    }
    
    /**
     * Assemble a URI for the specified path under the provided context. <br>
     * <b>NOTE</b>: The URI returned by this method uses the scheme, host, and port of the provided context URL
     * and specified path string.
     * 
     * @param context context URL
     * @param path path component
     * @return URI for the specified path within the provided context
     */
    public static URI uriForPath(final URL context, final String path) {
        return makeBasicURI(context.getProtocol(), context.getHost(), context.getPort(), path);
    }
    
    /**
     * Assemble a basic URI from the specified components.
     * 
     * @param scheme scheme name
     * @param host host name
     * @param port port number
     * @param path path
     * @return assembled basic URI
     */
    public static URI makeBasicURI(final String scheme, final String host, final int port, final String path) {
        try {
            return new URI(scheme, null, host, port, path, null, null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
}
