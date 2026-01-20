package tcpframework;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;

public class HTTPRequest {
    private final HTTPMethode method;
    private final String path;
    private final String httpVersion;
    private final Map<String, String[]> headers;
    private final byte[] bodyBytes;

    public HTTPRequest(HTTPMethode method, String path, String httpVersion, Map<String, String[]> headers, InputStream body) {
        this.method = method;
        this.path = path;
        this.httpVersion = httpVersion;
        this.headers = headers;
        try {
            this.bodyBytes = (body != null) ? body.readAllBytes() : new byte[0];
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public HTTPMethode getMethod() {
        return method;
    }

    public String getPath() {
        int queryIndex = path.indexOf('?');
        if (queryIndex != -1) {
            return path.substring(0, queryIndex);
        }
        return path;
    }

    public String getFullPath() {
        return path;
    }

    public String getQueryString() {
        int queryIndex = path.indexOf('?');
        if (queryIndex != -1 && queryIndex + 1 < path.length()) {
            return path.substring(queryIndex + 1);
        }
        return "";
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public Map<String, String[]> getHeaders() {
        return headers;
    }

    public InputStream getBody() {
        return new ByteArrayInputStream(bodyBytes);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(method.toString());
        sb.append(" ");
        sb.append(path);
        sb.append(" ");
        sb.append(httpVersion);
        sb.append("\n");
        for (Map.Entry<String, String[]> entry : headers.entrySet()) {
            sb.append(entry.getKey());
            sb.append(": ");
            sb.append(String.join(", ", entry.getValue()));
            sb.append("\n");
        }
        return sb.toString();
    }


}
