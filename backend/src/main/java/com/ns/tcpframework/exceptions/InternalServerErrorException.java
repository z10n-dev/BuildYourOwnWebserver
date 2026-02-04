package com.ns.tcpframework.exceptions;

/**
 * Exception thrown to indicate that an internal server error occurred.
 * This is used to signal that the server encountered an unexpected condition
 * that prevented it from fulfilling the request.
 */
public class InternalServerErrorException extends Exception{

    /**
     * Constructs a new InternalServerErrorException with the specified detail message.
     *
     * @param message The detail message explaining the reason for the exception.
     */
    public InternalServerErrorException(String message) {
        super(message);
    }
}
