package tcpframework;

import tcpframework.exceptions.BadRequestException;
import tcpframework.exceptions.NotImplementedException;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;

public class HTTPRequestParser {

    public static HTTPRequest parseHTTPRequest(Socket socket) throws Exception {
        InputStream rawIn = socket.getInputStream();
        String header = getHTTPHeader(rawIn);
        BufferedReader in = new BufferedReader(new StringReader(header));
        String line = in.readLine();
        if (line == null || line.isEmpty()) {
            throw new BadRequestException("Empty HTTP Request");
        }
        String[] requestLineParts = line.split(" ");

        HTTPMethode methode = extractMethode(requestLineParts);
        String path = extractPath(requestLineParts);
        String httpVersion = extractHttpVersion(requestLineParts);

        LinkedHashMap<String, String[]> headers = extractHttpHeaders(in);

        return new HTTPRequest(socket, methode, path, httpVersion, headers, bodyStream(rawIn, Integer.parseInt(headers.getOrDefault("content-length", new String[]{"0"})[0])));
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
        return new FixedLengthInputStream(in, contentLength);
    }

    private static HTTPMethode extractMethode(String[] line) throws Exception {
        HTTPMethode methode;

        if (line.length == 0 || line[0].isEmpty()) {
            throw new BadRequestException("Empty HTTP Methode");
        } else if (!line[0].equals(line[0].toUpperCase())) {
            throw new BadRequestException("Invalid HTTP Methode: " + line[0]);
        }
        try {
            methode = HTTPMethode.valueOf(line[0]);
        } catch (Exception e) {
            throw new NotImplementedException("Unsupported HTTP Methode: " + line[0]);
        }

        return methode;
    }

    private static String extractPath(String[] line) throws Exception {
        String path;

        if (line.length <= 1 || line[1].isEmpty()) {
            throw new BadRequestException("Empty HTTP Path");
        }

        path = line[1];

        return path;
    }

    private static String extractHttpVersion(String[] line) throws Exception {
        String httpVersion;

        if (line.length <= 2 || line[2].isEmpty()) {
            throw new BadRequestException("Empty HTTP Version");
        }

        httpVersion = line[2];

        return httpVersion;
    }

    private static LinkedHashMap<String, String[]> extractHttpHeaders(BufferedReader in) throws Exception {
        LinkedHashMap<String, String[]> headers = new LinkedHashMap<>();

        String line = in.readLine();
        while (line != null && !line.isEmpty()) {
            String[] parts = line.split(": ");

            if (parts.length == 0 || parts[0].isEmpty()) {
                throw new BadRequestException("Invalid HTTP Header: " + line);
            }

            String key = parts[0].toLowerCase();
            String[] values;

            if (parts.length > 1 && !parts[1].isEmpty()) {
                String value = parts[1];
                values = value.split(", ");

                if (headers.containsKey(key)) {
                    String[] existingValues = headers.get(key);
                    String[] newValues = new String[existingValues.length + values.length];
                    System.arraycopy(existingValues, 0, newValues, 0, existingValues.length);
                    System.arraycopy(values, 0, newValues, existingValues.length, values.length);
                    values = newValues;
                }
            } else {
                values = new String[]{};
            }

            headers.put(key, values);

            line = in.readLine();
        }

        return headers;
    }
}
