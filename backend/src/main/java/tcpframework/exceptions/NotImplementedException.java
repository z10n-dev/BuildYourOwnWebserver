package tcpframework.exceptions;

/**
 * Exception thrown to indicate that a requested operation or functionality
 * has not been implemented. This is used as a placeholder for
 * methods or features that are planned but not yet available.
 */
public class NotImplementedException extends Exception {

    /**
     * Constructs a new NotImplementedException with the specified detail message.
     *
     * @param message The detail message explaining the reason for the exception.
     */
    public NotImplementedException(String message) {
        super(message);
    }
}
