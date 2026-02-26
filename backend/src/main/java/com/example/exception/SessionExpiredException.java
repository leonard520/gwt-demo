package com.example.exception;

/**
 * Thrown if the session is no longer valid for a service request.
 * <p>
 * Rewritten from: com.example.client.exception.SessionTimedOutException
 * Changed: extends RuntimeException (was checked Exception + IsSerializable in GWT).
 * Renamed to SessionExpiredException per rewrite plan.
 * Default message preserved exactly from source.
 */
public class SessionExpiredException extends RuntimeException {

    private static final String DEFAULT_MSG = "The session has timed out.  You will need to login again.";

    /**
     * Constructor with default message.
     */
    public SessionExpiredException() {
        super(DEFAULT_MSG);
    }

    /**
     * Constructor with custom message.
     *
     * @param msg the error message
     */
    public SessionExpiredException(String msg) {
        super(msg);
    }

    /**
     * Constructor with cause.
     *
     * @param t the cause
     */
    public SessionExpiredException(Throwable t) {
        super(t);
    }

    /**
     * Constructor with message and cause.
     *
     * @param msg the error message
     * @param t   the cause
     */
    public SessionExpiredException(String msg, Throwable t) {
        super(msg, t);
    }
}
