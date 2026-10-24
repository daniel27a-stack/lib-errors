package cl.pirca.lib.errors;

import org.springframework.http.HttpStatusCode;

/**
 * Base of every pirca exception. A service extends it for each domain error, giving a stable
 * machine code (uppercase with underscores, such as {@code EXPENSE_NOT_FOUND}), a human message
 * and the HTTP status to answer with.
 */
public abstract class PircaException extends RuntimeException {

    private final String code;
    private final HttpStatusCode status;

    /**
     * Creates the exception.
     *
     * @param code the stable code for machines
     * @param message the human message
     * @param status the HTTP status of the answer
     */
    protected PircaException(String code, String message, HttpStatusCode status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    /**
     * Returns the stable code for machines.
     *
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns the HTTP status of the answer.
     *
     * @return the status
     */
    public HttpStatusCode getStatus() {
        return status;
    }
}
