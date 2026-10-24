package cl.pirca.lib.errors;

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Answers every error as a {@code ProblemDetail} (RFC 9457). It extends Spring's handler so that
 * the errors of Spring MVC itself (404, 405, 400...) keep their real status.
 */
@RestControllerAdvice
public class ErrorsControllerAdvice extends ResponseEntityExceptionHandler {

    private static final Log LOG = LogFactory.getLog(ErrorsControllerAdvice.class);

    /**
     * Answers a pirca exception with its own status, message and code.
     *
     * @param ex the exception
     * @return the answer
     */
    @ExceptionHandler(PircaException.class)
    public ResponseEntity<ProblemDetail> handlePirca(PircaException ex) {
        if (ex.getCause() != null) {
            LOG.warn("Pirca error " + ex.getCode() + " has a cause", ex.getCause());
        }
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
        HttpStatus standard = HttpStatus.resolve(ex.getStatus().value());
        problem.setTitle(standard != null ? standard.getReasonPhrase() : "Error");
        problem.setType(URI.create("urn:pirca:error:" + ex.getCode()));
        problem.setProperty("code", ex.getCode());
        return ResponseEntity.status(ex.getStatus()).body(problem);
    }

    /**
     * Answers any other exception with a generic 500; nothing of the exception leaves the service.
     *
     * @param ex the exception
     * @return the answer
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex) {
        LOG.error("Unexpected error", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
        problem.setTitle(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        problem.setType(URI.create("urn:pirca:error:internal"));
        problem.setProperty("code", "INTERNAL_ERROR");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}
