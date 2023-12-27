package org.example;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Request {
    private final String uri;
    private final String path;
    private final String method;

    public Request(String uri, String path, String method) {
        this.uri = uri;
        this.path = path;
        this.method = method;
    }

    public String getUri() {
        return uri;
    }

    public String getPath() {
        return path;
    }

    public String getMethod() {
        return method;
    }

    public String getQueryParam(String name) {
        List<NameValuePair> params = URLEncodedUtils.parse(URI.create(this.uri), StandardCharsets.UTF_8);
        for (NameValuePair param : params) {
            if (param.getName().equals(name)) {
                return param.getValue();
            }
        }
        return null;
    }

    public List<NameValuePair> getQueryParams() {
        return URLEncodedUtils.parse(URI.create(this.uri), StandardCharsets.UTF_8);
    }
}