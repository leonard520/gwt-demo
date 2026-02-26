package com.example.exception;

/**
 * Thrown if anything goes wrong with the item service operations.
 * <p>
 * Rewritten from: com.example.client.exception.ItemServiceException
 * Changed: extends RuntimeException (was checked Exception + IsSerializable in GWT).
 * Default message preserved exactly from source.
 */
public class ItemServiceException extends RuntimeException {

    private static final String DEFAULT_MSG = "Oops, we're having problems with the server.  Try again later.";

    /**
     * Constructor with default message.
     */
    public ItemServiceException() {
        super(DEFAULT_MSG);
    }

    /**
     * Constructor with custom message.
     *
     * @param message the error message
     */
    public ItemServiceException(String message) {
        super(message);
    }

    /**
     * Constructor with cause.
     *
     * @param cause the cause
     */
    public ItemServiceException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor with message and cause.
     *
     * @param message the error message
     * @param cause   the cause
     */
    public ItemServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
