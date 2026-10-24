package cl.pirca.lib.errors;

import org.springframework.http.HttpStatus;

/**
 * Ready-made exception answered with HTTP 409 (Conflict). The status is fixed by the class; whoever
 * throws it supplies the code and the message.
 */
public class ConflictException extends PircaException {

    /**
     * Creates the exception.
     *
     * @param code the stable code for machines
     * @param message the human message
     */
    public ConflictException(String code, String message) {
        super(code, message, HttpStatus.CONFLICT);
    }
}
