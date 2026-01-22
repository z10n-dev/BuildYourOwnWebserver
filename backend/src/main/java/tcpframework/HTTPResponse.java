package tcpframework;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class HTTPResponse {
    private int statusCode;
    private String statusMessage;
    private final HashMap<String, String[]> headers = new HashMap<>();
    private byte[] body;

    public HTTPResponse(int statusCode, String statusMessage) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

    public void setHeader(String key, String value) {
        if(headers.containsKey(key)){
            String[] values = headers.get(key);
            String[] newValues = new String[values.length + 1];
            System.arraycopy(values, 0, newValues, 0, values.length);
            newValues[values.length] = value;
            headers.put(key, newValues);
        } else {
            headers.put(key, new String[]{value});
        }
    }

    public void setBody(byte[] body, String contentType) {
        this.body = body;
        setHeader("Content-Length", String.valueOf(body.length));
        setHeader("Content-Type", contentType);
    }


    public void send(Socket socket) throws Exception{
        OutputStream out = socket.getOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        writer.write(String.format("HTTP/1.1 %d %s\r\n", statusCode, statusMessage));
        for (String key : headers.keySet()) {
            String[] values = headers.get(key);
            for (String value : values) {
                writer.write(String.format("%s: %s\r\n", key, value));
            }
        }
        writer.write("\r\n");
        writer.flush();
        if (body != null && body.length > 0) {
            out.write(body);
            out.flush();
        }
    }


}
