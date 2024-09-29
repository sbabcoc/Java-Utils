package com.nordstrom.common.uri;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
     * @param pathAndParams path component and query parameters
     * @return URI for the specified path and parameters within the provided context
     */
    public static URI uriForPath(final URL context, final String... pathAndParams) {
        return makeBasicURI(context.getProtocol(), context.getHost(), context.getPort(), pathAndParams);
    }
    
    /**
     * Assemble a basic URI from the specified components.
     * 
     * @param scheme scheme name
     * @param host host name
     * @param port port number
     * @param pathAndParams path and query parameters
     * @return assembled basic URI
     */
    public static URI makeBasicURI(final String scheme, final String host, final int port,
            final String... pathAndParams) {
        try {
            String path = (pathAndParams.length > 0) ? pathAndParams[0] : null;
            String query = null;
            if (pathAndParams.length > 1) {
                query = IntStream.range(1, pathAndParams.length).mapToObj(i -> {
                    try {
                        String param = pathAndParams[i];
                        int index = param.indexOf("=");
                        if (index == -1) return URLEncoder.encode(param, StandardCharsets.UTF_8.toString());
                        String key = URLEncoder.encode(param.substring(0, index), StandardCharsets.UTF_8.toString());
                        String val = URLEncoder.encode(param.substring(index + 1), StandardCharsets.UTF_8.toString());
                        return key + "=" + val;
                    } catch (Exception e) {
                        throw new IllegalArgumentException(e.getMessage(), e);
                    }
                }).collect(Collectors.joining("&"));
            }
            return new URI(scheme, null, host, port, path, query, null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
}
