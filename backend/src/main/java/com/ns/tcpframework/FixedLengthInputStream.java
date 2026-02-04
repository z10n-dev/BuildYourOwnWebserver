package com.ns.tcpframework;

import java.io.IOException;
import java.io.InputStream;

/**
 * A specialized InputStream that reads a fixed number of bytes from an underlying InputStream.
 * Once the specified number of bytes has been read, the stream behaves as if it has reached the end.
 */
public class FixedLengthInputStream extends InputStream {
    private final InputStream in;
    private long remaining;

    /**
     * Constructs a FixedLengthInputStream with the specified underlying InputStream and length.
     *
     * @param in     The underlying InputStream to read from.
     * @param length The maximum number of bytes to read from the underlying InputStream.
     */
    public FixedLengthInputStream(InputStream in, long length) {
        this.in = in;
        this.remaining = length;
    }

    /**
     * Reads the next byte of data from the stream. If the specified number of bytes has been read,
     * this method returns -1 to indicate the end of the stream.
     *
     * @return The next byte of data, or -1 if the end of the stream has been reached.
     * @throws IOException If an I/O error occurs while reading from the underlying InputStream.
     */
    @Override
    public int read() throws IOException {
        if (remaining <= 0) return -1;
        int data = in.read();
        if (data != -1) remaining--;
        return data;
    }
}
