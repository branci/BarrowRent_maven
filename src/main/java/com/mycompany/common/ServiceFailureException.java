package com.mycompany.common;

/**
 * This exception indicates service failure.
 *  
 * @author Branislav Smik <xsmik @fi.muni>
 */
public class ServiceFailureException extends RuntimeException {

    public ServiceFailureException(String msg) {
        super(msg);
    }

    public ServiceFailureException(Throwable cause) {
        super(cause);
    }

    public ServiceFailureException(String message, Throwable cause) {
        super(message, cause);
    }

}
