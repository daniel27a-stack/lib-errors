package cl.pirca.lib.errors;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/**
 * Registers the errors advice in servlet applications, unless the application defines its own.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
class ErrorsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    ErrorsControllerAdvice errorsControllerAdvice() {
        return new ErrorsControllerAdvice();
    }
}
