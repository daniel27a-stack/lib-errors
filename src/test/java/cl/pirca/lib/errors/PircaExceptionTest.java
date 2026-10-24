package cl.pirca.lib.errors;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

class PircaExceptionTest {

    @Test
    void aSubclassCarriesCodeMessageAndStatus() {
        TestException ex = new TestException();

        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getCode()).isEqualTo("TEST_FAILED");
        assertThat(ex.getMessage()).isEqualTo("The test failed.");
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
    }

    static class TestException extends PircaException {
        TestException() {
            super("TEST_FAILED", "The test failed.", HttpStatusCode.valueOf(409));
        }
    }
}
