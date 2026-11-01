package cl.pirca.lib.errors;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import cl.pirca.lib.logging.RequestLoggingFilter;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.annotation.ImportCandidates;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ExtendWith(OutputCaptureExtension.class)
class ErrorsAutoConfigurationIntTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ErrorsAutoConfiguration.class));

    @Test
    void registersTheAdvice() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(ErrorsControllerAdvice.class));
    }

    @Test
    void aConsumerBeanReplacesTheAdvice() {
        contextRunner.withUserConfiguration(CustomAdviceConfiguration.class).run(context -> {
            assertThat(context).hasSingleBean(ErrorsControllerAdvice.class);
            assertThat(context.getBean(ErrorsControllerAdvice.class))
                    .isSameAs(CustomAdviceConfiguration.CUSTOM);
        });
    }

    @Test
    void withoutLibLoggingTheGeneric500IsLoggedOnce(CapturedOutput output) {
        contextRunner.withClassLoader(new FilteredClassLoader(RequestLoggingFilter.class)).run(context -> {
            assertThat(context).hasSingleBean(ErrorsControllerAdvice.class);
            callBoom(context.getBean(ErrorsControllerAdvice.class));
        });
        assertThat(output.getAll().split("Unexpected error", -1)).hasSize(2);
    }

    @Test
    void withLibLoggingTheAdviceDoesNotLog(CapturedOutput output) {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ErrorsControllerAdvice.class);
            callBoom(context.getBean(ErrorsControllerAdvice.class));
        });
        assertThat(output.getAll()).doesNotContain("Unexpected error");
    }

    @Test
    void aConsumerBeanReplacesTheAdviceWithoutLibLogging() {
        contextRunner.withClassLoader(new FilteredClassLoader(RequestLoggingFilter.class))
                .withUserConfiguration(CustomAdviceConfiguration.class).run(context -> {
                    assertThat(context).hasSingleBean(ErrorsControllerAdvice.class);
                    assertThat(context.getBean(ErrorsControllerAdvice.class))
                            .isSameAs(CustomAdviceConfiguration.CUSTOM);
                });
    }

    private static void callBoom(ErrorsControllerAdvice advice) throws Exception {
        MockMvcBuilders.standaloneSetup(new BoomController()).setControllerAdvice(advice).build()
                .perform(get("/boom"));
    }

    @RestController
    static class BoomController {

        @GetMapping("/boom")
        String boom() {
            throw new IllegalStateException("boom");
        }
    }

    @Test
    void doesNotApplyOutsideAServletApplication() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ErrorsAutoConfiguration.class))
                .run(context -> assertThat(context).doesNotHaveBean(ErrorsControllerAdvice.class));
    }

    @Test
    void importsFileListsOnlyTheRealAutoConfiguration() {
        List<String> candidates = ImportCandidates.load(AutoConfiguration.class, getClass().getClassLoader())
                .getCandidates().stream().filter(name -> name.startsWith("cl.pirca.")).toList();
        assertThat(candidates).containsExactly("cl.pirca.lib.errors.ErrorsAutoConfiguration");
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomAdviceConfiguration {

        static final ErrorsControllerAdvice CUSTOM = new ErrorsControllerAdvice();

        @Bean
        ErrorsControllerAdvice customAdvice() {
            return CUSTOM;
        }
    }
}
