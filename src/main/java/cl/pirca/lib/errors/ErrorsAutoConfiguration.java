package cl.pirca.lib.errors;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the errors advice in servlet applications, unless the application defines its own.
 * With {@code lib-logging} on the classpath the advice does not log (lib-logging does); without it
 * the advice logs the generic 500 itself, so that it never goes unlogged.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
class ErrorsAutoConfiguration {

    private static final String LOGGING_FILTER = "cl.pirca.lib.logging.RequestLoggingFilter";

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = LOGGING_FILTER)
    static class WithLogging {

        @Bean
        @ConditionalOnMissingBean
        ErrorsControllerAdvice errorsControllerAdvice() {
            return new ErrorsControllerAdvice(false);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingClass(LOGGING_FILTER)
    static class WithoutLogging {

        @Bean
        @ConditionalOnMissingBean
        ErrorsControllerAdvice errorsControllerAdvice() {
            return new ErrorsControllerAdvice(true);
        }
    }
}
