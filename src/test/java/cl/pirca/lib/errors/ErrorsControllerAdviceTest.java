package cl.pirca.lib.errors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

class ErrorsControllerAdviceTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new ErrorsControllerAdvice())
                .build();
    }

    @Test
    void aPircaExceptionIsAnsweredAsProblemDetail() throws Exception {
        mockMvc.perform(get("/pirca"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("The test failed."))
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.type").value("urn:pirca:error:TEST_FAILED"))
                .andExpect(jsonPath("$.code").value("TEST_FAILED"))
                .andExpect(jsonPath("$.traceId").doesNotExist());
    }

    @Test
    void anUnexpectedExceptionIsAnsweredAsGeneric500() throws Exception {
        String body = mockMvc.perform(get("/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred."))
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.stackTrace").doesNotExist())
                .andExpect(jsonPath("$.exception").doesNotExist())
                .andExpect(jsonPath("$.traceId").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        assertThat(body).doesNotContain("secret-internal-detail").doesNotContain("TestController");
    }

    @Test
    void aMissingRouteStaysA404() throws Exception {
        mockMvc.perform(get("/missing"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @RestController
    static class TestController {

        @GetMapping("/pirca")
        String pirca() {
            throw new TestException();
        }

        @GetMapping("/boom")
        String boom() {
            throw new IllegalStateException("secret-internal-detail");
        }
    }

    static class TestException extends PircaException {
        TestException() {
            super("TEST_FAILED", "The test failed.", HttpStatusCode.valueOf(409));
        }
    }
}
