package co.franquicia.api.error;

import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class ErrorHandlerConfig {

    /**
     * Registra el manejador global de errores
     * Usa el ObjectMapper que ya está configurado en ObjectMapperConfig
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ErrorWebExceptionHandler globalErrorHandler(ObjectMapper objectMapper) {
        return new GlobalErrorHandler(objectMapper);
    }
}