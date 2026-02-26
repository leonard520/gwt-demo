package com.example.exception;

/**
 * Thrown if anything goes wrong with user authentication.
 * <p>
 * Rewritten from: com.example.client.exception.LoginFailureException
 * Changed: extends RuntimeException (was checked Exception + IsSerializable in GWT).
 * Default message preserved exactly from source.
 */
public class LoginFailureException extends RuntimeException {

    private static final String DEFAULT_MSG = "The email or password you entered is incorrect.";

    /**
     * Constructor with default message.
     */
    public LoginFailureException() {
        super(DEFAULT_MSG);
    }

    /**
     * Constructor with custom message.
     *
     * @param msg the error message
     */
    public LoginFailureException(String msg) {
        super(msg);
    }

    /**
     * Constructor with cause.
     *
     * @param t the cause
     */
    public LoginFailureException(Throwable t) {
        super(t);
    }

    /**
     * Constructor with message and cause.
     *
     * @param msg the error message
     * @param t   the cause
     */
    public LoginFailureException(String msg, Throwable t) {
        super(msg, t);
    }
}
