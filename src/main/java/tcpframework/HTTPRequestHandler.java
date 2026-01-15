package tcpframework;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;

public class HTTPRequestHandler {

    public HTTPRequest parseHTTPRequest(InputStream rawIn) throws IOException {
        String header = getHTTPHeader(rawIn);
        BufferedReader in = new BufferedReader(new StringReader(header));
        String line = in.readLine();

        HTTPMethode methode = HTTPMethode.valueOf(line.split(" ")[0]);
        String path = line.split(" ")[1];
        String httpVersion = line.split(" ")[2];

        LinkedHashMap<String, String> headers = new LinkedHashMap<>();

        line = in.readLine();

        while (line != null && !line.isEmpty()) {
            String key = line.split(": ")[0];
            if ((line.length() >= key.length()+2)) {
                String value = line.split(": ")[1];
                headers.put(key, value);
            } else {
                headers.put(key, "");
            }
            line = in.readLine();
        }

        return new HTTPRequest(methode, path, httpVersion, headers, bodyStream(rawIn, Integer.parseInt(headers.getOrDefault("Content-Length", "0"))));
    }

    private String getHTTPHeader(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int b;
        int lastState = 0;

        while ((b = in.read()) != -1) {
            buffer.write(b);

            if (lastState == 0 && b == '\r') {
                lastState = 1;
            } else if (lastState == 1 && b == '\n') {
                lastState = 2;
            } else if (lastState == 2 && b == '\r') {
                lastState = 3;
            } else if (lastState == 3 && b == '\n') {
                break; // End of headers -> found \r\n\r\n
            } else {
                lastState = 0;
            }
        }

        return buffer.toString(StandardCharsets.UTF_8);
    }

    private InputStream bodyStream(InputStream in, int contentLength) {
        return new FixedLenghtInputStream(in, contentLength);
    }
}
