package cl.pirca.lib.errors;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.annotation.ImportCandidates;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
