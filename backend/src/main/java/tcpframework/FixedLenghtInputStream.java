package tcpframework;

import java.io.IOException;
import java.io.InputStream;

public class FixedLenghtInputStream extends InputStream {
    private final InputStream in;
    private long remaining;

    public FixedLenghtInputStream(InputStream in, long length) {
        this.in = in;
        this.remaining = length;
    }

    @Override
    public int read() throws IOException {
        if (remaining <= 0) return -1;
        int data = in.read();
        if (data != -1) remaining--;
        return data;
    }
}
