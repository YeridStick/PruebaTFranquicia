package co.franquicia.api.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration
public class ErrorHandlerConfig {

    /**
     * Configura un ObjectMapper de Jackson para serializar respuestas de error
     * @return ObjectMapper configurado
     */
    @Bean
    public ObjectMapper jacksonObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Agregar soporte para Java Time (LocalDateTime, Instant, etc)
        mapper.registerModule(new JavaTimeModule());

        // No serializar fechas como timestamps
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper;
    }

    /**
     * Registra el manejador global de errores
     * @param jacksonObjectMapper ObjectMapper para serializar respuestas de error
     * @return ErrorWebExceptionHandler configurado
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ErrorWebExceptionHandler globalErrorHandler(ObjectMapper jacksonObjectMapper) {
        return new GlobalErrorHandler(jacksonObjectMapper);
    }
}