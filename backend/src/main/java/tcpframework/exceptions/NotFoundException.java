package tcpframework.exceptions;

/**
 * Exception thrown to indicate that a requested resource was not found.
 * This is used to signal that the server could not find the requested resource.
 */
public class NotFoundException extends Exception{

    /**
     * Constructs a new NotFoundException with the specified detail message.
     *
     * @param message The detail message explaining the reason for the exception.
     */
    public NotFoundException(String message) {
        super(message);
    }
}
