package co.franquicia.api.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration
public class ErrorHandlerConfig {

    /**
     * Registra el manejador global de errores
     * @param objectMapper ObjectMapper para serializar respuestas de error
     * @return ErrorWebExceptionHandler configurado
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ErrorWebExceptionHandler globalErrorHandler(ObjectMapper objectMapper) {
        return new GlobalErrorHandler(objectMapper);
    }
}