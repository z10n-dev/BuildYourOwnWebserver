package tcpframework;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;

public class HTTPRequestHandler {

    public static HTTPRequest parseHTTPRequest(Socket socket) throws Exception{
            InputStream rawIn = socket.getInputStream();
            String header = getHTTPHeader(rawIn);
            BufferedReader in = new BufferedReader(new StringReader(header));
            String line = in.readLine();
            String[] requestLineParts = line.split(" ");

            HTTPMethode methode = extractMethode(socket, requestLineParts);
            String path = extractPath(socket, requestLineParts);
            String httpVersion = extractHttpVersion(socket, requestLineParts);

            LinkedHashMap<String, String[]> headers = extractHttpHeaders(socket, in);

            return new HTTPRequest(methode, path, httpVersion, headers, bodyStream(rawIn, Integer.parseInt(headers.getOrDefault("Content-Length", new String[]{"0"})[0])));
    }

    private static String getHTTPHeader(InputStream in) throws IOException {
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

    private static InputStream bodyStream(InputStream in, int contentLength) {
        return new FixedLenghtInputStream(in, contentLength);
    }

    private static HTTPMethode extractMethode(Socket socket, String[] line) throws Exception {
        HTTPMethode methode;

            if (line.length == 0 || line[0].isEmpty()){
                HTTPErrorHandler.sendBadRequest(socket);
                throw new Exception("Empty Header line request line");
            } else if (!line[0].equals(line[0].toUpperCase())){
                HTTPErrorHandler.sendBadRequest(socket);
                throw new Exception("Invalid request line casing");
            }
            try {
                methode = HTTPMethode.valueOf(line[0]);
            } catch (Exception e){
                HTTPErrorHandler.sendNotImplemented(socket);
                throw new Exception("Unsupported HTTP Methode: " + line[0]);
            }

        return methode;
    }

    private static String extractPath(Socket socket, String[] line) throws Exception {
        String path;
        try {
            path = line[1];
        } catch (Exception e) {
            HTTPErrorHandler.sendBadRequest(socket);
            throw new Exception("Invalid path line request line");
        }
        return path;
    }

    private static String extractHttpVersion(Socket socket, String[] line) throws Exception {
        String httpVersion;
        try {
            httpVersion = line[2];
        } catch (Exception e) {
            HTTPErrorHandler.sendBadRequest(socket);
            throw new Exception("Invalid request line HTTP Version");
        }
        return httpVersion;
    }

    private static LinkedHashMap<String, String[]> extractHttpHeaders(Socket socket, BufferedReader in) throws Exception {
        LinkedHashMap<String, String[]> headers = new LinkedHashMap<>();

        try {
            String line = in.readLine();
            while (line != null && !line.isEmpty()) {
                String key = line.split(": ")[0];
                if ((line.length() >= key.length() + 2)) {
                    String value = line.split(": ")[1];
                    String[] values = value.split(", ");
                    headers.put(key, values);
                }
                line = in.readLine();
            }

            return headers;
        } catch (Exception e) {
            HTTPErrorHandler.sendBadRequest(socket);
            throw new Exception("Invalid header line");
        }
    }
}
