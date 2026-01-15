package tcpframework;

import java.io.InputStream;
import java.util.Map;

public class HTTPRequest {
    private final HTTPMethode method;
    private final String path;
    private final String httpVersion;
    private final Map<String, String> headers;
    private final InputStream body;

    public HTTPRequest(HTTPMethode method, String path, String httpVersion, Map<String, String> headers, InputStream body) {
        this.method = method;
        this.path = path;
        this.httpVersion = httpVersion;
        this.headers = headers;
        this.body = body;
    }

    public HTTPMethode getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public InputStream getBody() {
        return body;
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
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append(entry.getKey());
            sb.append(": ");
            sb.append(entry.getValue());
            sb.append("\n");
        }
        return sb.toString();
    }


}
