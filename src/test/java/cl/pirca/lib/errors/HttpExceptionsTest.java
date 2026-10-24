package cl.pirca.lib.errors;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.BiFunction;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;

class HttpExceptionsTest {

    private static final String CODE = "SAMPLE_CODE";
    private static final String MESSAGE = "Sample message.";

    static Stream<Arguments> cases() {
        return Stream.of(
                of(BadRequestException.class, BadRequestException::new, HttpStatus.BAD_REQUEST),
                of(ForbiddenException.class, ForbiddenException::new, HttpStatus.FORBIDDEN),
                of(NotFoundException.class, NotFoundException::new, HttpStatus.NOT_FOUND),
                of(ConflictException.class, ConflictException::new, HttpStatus.CONFLICT));
    }

    private static Arguments of(Class<?> type, BiFunction<String, String, PircaException> factory,
            HttpStatus status) {
        return Arguments.of(Named.of(type.getSimpleName(), type), factory, status);
    }

    @ParameterizedTest
    @MethodSource("cases")
    void carriesTheGivenCodeAndMessageAndTheFixedStatus(Class<?> type,
            BiFunction<String, String, PircaException> factory, HttpStatus status) {
        PircaException exception = factory.apply(CODE, MESSAGE);

        assertThat(exception).isInstanceOf(type).isInstanceOf(RuntimeException.class);
        assertThat(exception.getCode()).isEqualTo(CODE);
        assertThat(exception.getMessage()).isEqualTo(MESSAGE);
        assertThat(exception.getStatus()).isEqualTo(status);
    }

    @ParameterizedTest
    @MethodSource("cases")
    void hasASinglePublicConstructorWithoutStatus(Class<?> type,
            BiFunction<String, String, PircaException> factory, HttpStatus status) {
        assertThat(type.getConstructors()).hasSize(1);
        assertThat(type.getConstructors()[0].getParameterTypes())
                .containsExactly(String.class, String.class);
    }
}
