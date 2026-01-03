package co.franquicia.r2dbc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adapters.r2dbc.connection-pool")
public record R2dbcPoolProperties(
        Integer initialSize,
        Integer maxSize,
        Integer maxIdleTime // minutos
) {}
