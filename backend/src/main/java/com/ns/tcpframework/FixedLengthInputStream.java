package com.ns.tcpframework;

import java.io.IOException;
import java.io.InputStream;

/**
 * A specialized InputStream that reads a fixed number of bytes from an underlying InputStream.
 * <p>
 * This class acts as a limiting wrapper around another InputStream, restricting the number
 * of bytes that can be read to a predetermined length. Once the specified number of bytes
 * has been read, the stream behaves as if it has reached the end-of-stream (EOF), even if
 * the underlying stream has more data available.
 * <p>
 * This is particularly useful for:
 * <ul>
 *   <li>Reading HTTP request bodies with Content-Length headers</li>
 *   <li>Processing fixed-size chunks in streaming protocols</li>
 *   <li>Preventing over-reading from shared or multiplexed streams</li>
 *   <li>Enforcing size limits on incoming data</li>
 * </ul>
 * <p>
 * Thread-safety: This class is not thread-safe. External synchronization is required
 * if an instance is accessed by multiple threads concurrently.
 * <p>
 * Example usage:
 * <pre>
 * InputStream socket = clientSocket.getInputStream();
 * InputStream limitedStream = new FixedLengthInputStream(socket, 1024);
 * // Can only read up to 1024 bytes, even if socket has more data
 * </pre>
 *
 * @see InputStream
 */
public class FixedLengthInputStream extends InputStream {
    /**
     * The underlying input stream from which data is read.
     */
    private final InputStream in;

    /**
     * The number of bytes remaining to be read before reaching the artificial end-of-stream.
     * <p>
     * This value decrements with each successful read operation and determines when
     * the stream should report EOF.
     */
    private long remaining;

    /**
     * Constructs a FixedLengthInputStream with the specified underlying InputStream and length.
     * <p>
     * The constructed stream will allow reading at most {@code length} bytes from the
     * underlying stream. Subsequent read attempts after consuming {@code length} bytes
     * will return -1 (end-of-stream).
     *
     * @param in     The underlying InputStream to read from. Must not be null.
     * @param length The maximum number of bytes to read from the underlying InputStream.
     *               Should be non-negative. A value of 0 creates a stream that is
     *               immediately at end-of-stream.
     * @throws NullPointerException if {@code in} is null.
     */
    public FixedLengthInputStream(InputStream in, long length) {
        this.in = in;
        this.remaining = length;
    }

    /**
     * Reads the next byte of data from the stream.
     * <p>
     * If the specified number of bytes has been read (i.e., {@code remaining} reaches 0),
     * this method returns -1 to indicate the end of the stream, even if the underlying
     * stream has more data available.
     * <p>
     * This method blocks until input data is available, the end of the stream is detected,
     * or an exception is thrown.
     *
     * @return The next byte of data as an integer in the range 0 to 255, or -1 if the
     *         end of the stream has been reached (either the fixed length limit was reached
     *         or the underlying stream ended).
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
