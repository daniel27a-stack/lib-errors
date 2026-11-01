package cl.pirca.lib.errors;

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Answers every error as a {@code ProblemDetail} (RFC 9457). It extends Spring's handler so that
 * the errors of Spring MVC itself (404, 405, 400...) keep their real status.
 */
@RestControllerAdvice
public class ErrorsControllerAdvice extends ResponseEntityExceptionHandler {

    /**
     * Name of the request attribute that holds the exception this advice answered. It is the
     * contract with {@code lib-logging}, which reads it.
     */
    public static final String EXCEPTION_ATTRIBUTE = "cl.pirca.lib.errors.EXCEPTION";

    private static final Log LOG = LogFactory.getLog(ErrorsControllerAdvice.class);

    private final boolean logUnexpected;

    /**
     * Creates the advice without any logging: the exception is only left in the request attribute.
     */
    public ErrorsControllerAdvice() {
        this(false);
    }

    /**
     * Creates the advice.
     *
     * @param logUnexpected whether the generic 500 is logged here (for services without
     *                      {@code lib-logging}, which would otherwise leave no log of it)
     */
    public ErrorsControllerAdvice(boolean logUnexpected) {
        this.logUnexpected = logUnexpected;
    }

    /**
     * Answers a pirca exception with its own status, message and code.
     *
     * @param ex the exception
     * @param request the current request
     * @return the answer
     */
    @ExceptionHandler(PircaException.class)
    public ResponseEntity<ProblemDetail> handlePirca(PircaException ex, WebRequest request) {
        store(ex, request);
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
     * @param request the current request
     * @return the answer
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex, WebRequest request) {
        store(ex, request);
        if (logUnexpected) {
            LOG.error("Unexpected error", ex);
        }
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
        problem.setTitle(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        problem.setType(URI.create("urn:pirca:error:internal"));
        problem.setProperty("code", "INTERNAL_ERROR");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }

    /**
     * Stores the exception of every Spring MVC error answered by the parent handlers, then answers
     * as Spring does.
     *
     * @param ex the exception
     * @param body the body of the answer
     * @param headers the headers of the answer
     * @param statusCode the status of the answer
     * @param request the current request
     * @return the answer
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body,
            HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        store(ex, request);
        return super.handleExceptionInternal(ex, body, headers, statusCode, request);
    }

    private static void store(Exception ex, WebRequest request) {
        request.setAttribute(EXCEPTION_ATTRIBUTE, ex, RequestAttributes.SCOPE_REQUEST);
    }
}
