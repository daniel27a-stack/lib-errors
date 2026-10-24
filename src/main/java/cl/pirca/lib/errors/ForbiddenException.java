package cl.pirca.lib.errors;

import org.springframework.http.HttpStatus;

/**
 * Ready-made exception answered with HTTP 403 (Forbidden). The status is fixed by the class; whoever
 * throws it supplies the code and the message.
 */
public class ForbiddenException extends PircaException {

    /**
     * Creates the exception.
     *
     * @param code the stable code for machines
     * @param message the human message
     */
    public ForbiddenException(String code, String message) {
        super(code, message, HttpStatus.FORBIDDEN);
    }

    /**
     * Creates the exception with the original exception that caused it. The cause is logged, never
     * sent in the answer.
     *
     * @param code the stable code for machines
     * @param message the human message
     * @param cause the original exception, may be {@code null}
     */
    public ForbiddenException(String code, String message, Throwable cause) {
        super(code, message, HttpStatus.FORBIDDEN, cause);
    }
}
