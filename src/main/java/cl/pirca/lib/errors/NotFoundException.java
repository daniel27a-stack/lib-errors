package cl.pirca.lib.errors;

import org.springframework.http.HttpStatus;

/**
 * Ready-made exception answered with HTTP 404 (Not Found). The status is fixed by the class; whoever
 * throws it supplies the code and the message.
 */
public class NotFoundException extends PircaException {

    /**
     * Creates the exception.
     *
     * @param code the stable code for machines
     * @param message the human message
     */
    public NotFoundException(String code, String message) {
        super(code, message, HttpStatus.NOT_FOUND);
    }

    /**
     * Creates the exception with the original exception that caused it. The cause is logged, never
     * sent in the answer.
     *
     * @param code the stable code for machines
     * @param message the human message
     * @param cause the original exception, may be {@code null}
     */
    public NotFoundException(String code, String message, Throwable cause) {
        super(code, message, HttpStatus.NOT_FOUND, cause);
    }
}
