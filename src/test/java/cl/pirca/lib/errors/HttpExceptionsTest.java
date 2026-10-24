package cl.pirca.lib.errors;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
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

    private interface CauseFactory {
        PircaException create(String code, String message, Throwable cause);
    }

    static Stream<Arguments> cases() {
        return Stream.of(
                of(BadRequestException.class, BadRequestException::new, BadRequestException::new,
                        HttpStatus.BAD_REQUEST),
                of(ForbiddenException.class, ForbiddenException::new, ForbiddenException::new,
                        HttpStatus.FORBIDDEN),
                of(NotFoundException.class, NotFoundException::new, NotFoundException::new,
                        HttpStatus.NOT_FOUND),
                of(ConflictException.class, ConflictException::new, ConflictException::new,
                        HttpStatus.CONFLICT));
    }

    private static Arguments of(Class<?> type, BiFunction<String, String, PircaException> factory,
            CauseFactory causeFactory, HttpStatus status) {
        return Arguments.of(Named.of(type.getSimpleName(), type), factory, causeFactory, status);
    }

    @ParameterizedTest
    @MethodSource("cases")
    void carriesTheGivenCodeAndMessageAndTheFixedStatus(Class<?> type,
            BiFunction<String, String, PircaException> factory, CauseFactory causeFactory,
            HttpStatus status) {
        PircaException exception = factory.apply(CODE, MESSAGE);

        assertThat(exception).isInstanceOf(type).isInstanceOf(RuntimeException.class);
        assertThat(exception.getCode()).isEqualTo(CODE);
        assertThat(exception.getMessage()).isEqualTo(MESSAGE);
        assertThat(exception.getStatus()).isEqualTo(status);
        assertThat(exception.getCause()).isNull();
    }

    @ParameterizedTest
    @MethodSource("cases")
    void carriesTheCauseAndKeepsTheFixedStatus(Class<?> type,
            BiFunction<String, String, PircaException> factory, CauseFactory causeFactory,
            HttpStatus status) {
        IllegalStateException cause = new IllegalStateException("original");
        PircaException exception = causeFactory.create(CODE, MESSAGE, cause);

        assertThat(exception).isInstanceOf(type);
        assertThat(exception.getCause()).isSameAs(cause);
        assertThat(exception.getCode()).isEqualTo(CODE);
        assertThat(exception.getMessage()).isEqualTo(MESSAGE);
        assertThat(exception.getStatus()).isEqualTo(status);
    }

    @ParameterizedTest
    @MethodSource("cases")
    void hasTheConstructorsWithoutStatus(Class<?> type,
            BiFunction<String, String, PircaException> factory, CauseFactory causeFactory,
            HttpStatus status) {
        Set<java.util.List<Class<?>>> shapes = java.util.Arrays.stream(type.getConstructors())
                .map(c -> java.util.List.<Class<?>>of(c.getParameterTypes()))
                .collect(java.util.stream.Collectors.toSet());
        assertThat(shapes).containsExactlyInAnyOrder(
                java.util.List.of(String.class, String.class),
                java.util.List.of(String.class, String.class, Throwable.class));
    }
}
