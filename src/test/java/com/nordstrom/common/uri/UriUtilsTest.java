package com.nordstrom.common.uri;

import static org.testng.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.testng.annotations.Test;

public class UriUtilsTest {

    @Test
    public void testUriWithoutPath() throws MalformedURLException {
        URL context = URI.create("http://user:pswd@host.com:80/context/path?id=123#frag").toURL();
        URI pathURI = UriUtils.uriForPath(context);
        assertEquals(pathURI.getScheme(), "http", "Scheme mismatch");
        assertEquals(pathURI.getUserInfo(), null, "User info mismatch");
        assertEquals(pathURI.getHost(), "host.com", "Host mismatch");
        assertEquals(pathURI.getPort(), 80, "Post mismatch");
        assertEquals(pathURI.getPath(), "", "Path mismatch");
        assertEquals(pathURI.getQuery(), null, "Query mismatch");
        assertEquals(pathURI.getFragment(), null, "Fragment mismatch");
    }

    @Test
    public void testUriForPath() throws MalformedURLException {
        URL context = URI.create("http://user:pswd@host.com:80/context/path?id=123#frag").toURL();
        URI pathURI = UriUtils.uriForPath(context, "/target");
        assertEquals(pathURI.getScheme(), "http", "Scheme mismatch");
        assertEquals(pathURI.getUserInfo(), null, "User info mismatch");
        assertEquals(pathURI.getHost(), "host.com", "Host mismatch");
        assertEquals(pathURI.getPort(), 80, "Post mismatch");
        assertEquals(pathURI.getPath(), "/target", "Path mismatch");
        assertEquals(pathURI.getQuery(), null, "Query mismatch");
        assertEquals(pathURI.getFragment(), null, "Fragment mismatch");
    }

    @Test
    public void testUriForPathAndParams() throws MalformedURLException {
        URL context = URI.create("http://user:pswd@host.com:80/context/path?id=123#frag").toURL();
        URI pathURI = UriUtils.uriForPath(context, "/target", "id=321", "q=fresh & clean");
        assertEquals(pathURI.getScheme(), "http", "Scheme mismatch");
        assertEquals(pathURI.getUserInfo(), null, "User info mismatch");
        assertEquals(pathURI.getHost(), "host.com", "Host mismatch");
        assertEquals(pathURI.getPort(), 80, "Post mismatch");
        assertEquals(pathURI.getPath(), "/target", "Path mismatch");
        assertEquals(pathURI.getQuery(), "id=321&q=fresh+%26+clean", "Query mismatch");
        assertEquals(pathURI.getFragment(), null, "Fragment mismatch");
    }

    @Test
    public void testMakeBasicURI() {
        URI basicURI = UriUtils.makeBasicURI("http", "host.com", 80);
        assertEquals(basicURI.getScheme(), "http", "Scheme mismatch");
        assertEquals(basicURI.getUserInfo(), null, "User info mismatch");
        assertEquals(basicURI.getHost(), "host.com", "Host mismatch");
        assertEquals(basicURI.getPort(), 80, "Post mismatch");
        assertEquals(basicURI.getPath(), "", "Path mismatch");
        assertEquals(basicURI.getQuery(), null, "Query mismatch");
        assertEquals(basicURI.getFragment(), null, "Fragment mismatch");
    }

    @Test
    public void testMakeBasicURIWithPath() {
        URI basicURI = UriUtils.makeBasicURI("http", "host.com", 80, "/target");
        assertEquals(basicURI.getScheme(), "http", "Scheme mismatch");
        assertEquals(basicURI.getUserInfo(), null, "User info mismatch");
        assertEquals(basicURI.getHost(), "host.com", "Host mismatch");
        assertEquals(basicURI.getPort(), 80, "Post mismatch");
        assertEquals(basicURI.getPath(), "/target", "Path mismatch");
        assertEquals(basicURI.getQuery(), null, "Query mismatch");
        assertEquals(basicURI.getFragment(), null, "Fragment mismatch");
    }

    @Test
    public void testMakeBasicURIWithPathAndParams() {
        URI basicURI = UriUtils.makeBasicURI("http", "host.com", 80, "/target", "id=123", "q=fresh & clean");
        assertEquals(basicURI.getScheme(), "http", "Scheme mismatch");
        assertEquals(basicURI.getUserInfo(), null, "User info mismatch");
        assertEquals(basicURI.getHost(), "host.com", "Host mismatch");
        assertEquals(basicURI.getPort(), 80, "Post mismatch");
        assertEquals(basicURI.getPath(), "/target", "Path mismatch");
        assertEquals(basicURI.getQuery(), "id=123&q=fresh+%26+clean", "Query mismatch");
        assertEquals(basicURI.getFragment(), null, "Fragment mismatch");
    }
}
