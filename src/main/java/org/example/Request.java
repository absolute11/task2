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
    private final List<NameValuePair> queryParams; // Добавлено поле для хранения параметров запроса

    public Request(String uri, String path, String method) {
        this.uri = uri;
        this.path = path;
        this.method = method;
        this.queryParams = parseQueryParams(uri); // Вызов метода для парсинга параметров запроса
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
        for (NameValuePair param : queryParams) {
            if (param.getName().equals(name)) {
                return param.getValue();
            }
        }
        return null;
    }

    public List<NameValuePair> getQueryParams() {
        return queryParams;
    }

    private List<NameValuePair> parseQueryParams(String uri) {
        return URLEncodedUtils.parse(URI.create(uri), StandardCharsets.UTF_8);
    }
}