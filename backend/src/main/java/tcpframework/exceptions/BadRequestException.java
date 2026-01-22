package tcpframework.exceptions;

/**
 * Exception thrown to indicate that a bad request was made.
 * This is used to signal that the client sent an invalid or malformed request.
 */
public class BadRequestException extends Exception{

    /**
     * Constructs a new BadRequestException with the specified detail message.
     *
     * @param message The detail message explaining the reason for the exception.
     */
    public BadRequestException(String message) {
        super(message);
    }
}
