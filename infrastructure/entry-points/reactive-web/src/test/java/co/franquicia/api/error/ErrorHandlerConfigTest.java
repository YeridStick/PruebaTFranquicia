package co.franquicia.api.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorHandlerConfigTest {

    private final ErrorHandlerConfig config = new ErrorHandlerConfig();

    @Test
    void jacksonObjectMapper_shouldRegisterJavaTimeModuleAndDisableTimestamps() throws Exception {
        ObjectMapper mapper = config.jacksonObjectMapper();

        assertThat(mapper.getRegisteredModuleIds())
                .contains("jackson-datatype-jsr310");

        assertThat(mapper.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)).isFalse();

        String serialized = mapper.writeValueAsString(Instant.parse("2020-01-01T00:00:00Z"));
        assertThat(serialized).isEqualTo("\"2020-01-01T00:00:00Z\"");
    }

    @Test
    void globalErrorHandler_buildsHandler() {
        ErrorWebExceptionHandler handler = config.globalErrorHandler(config.jacksonObjectMapper());
        assertThat(handler).isInstanceOf(GlobalErrorHandler.class);
    }
}
