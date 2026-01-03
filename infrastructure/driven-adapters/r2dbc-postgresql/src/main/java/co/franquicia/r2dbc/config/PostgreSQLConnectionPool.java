package co.franquicia.r2dbc.config;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties({PostgresqlConnectionProperties.class, R2dbcPoolProperties.class})
public class PostgreSQLConnectionPool {

    @Bean
    public ConnectionPool connectionPool(PostgresqlConnectionProperties properties,
                                         R2dbcPoolProperties poolProps) {

        PostgresqlConnectionConfiguration dbConfiguration =
                PostgresqlConnectionConfiguration.builder()
                        .host(properties.host())
                        .port(properties.port())
                        .database(properties.database())
                        .schema(properties.schema())
                        .username(properties.username())
                        .password(properties.password())
                        .build();

        ConnectionPoolConfiguration poolConfiguration =
                ConnectionPoolConfiguration.builder()
                        .connectionFactory(new PostgresqlConnectionFactory(dbConfiguration))
                        .name("api-postgres-connection-pool")
                        .initialSize(poolProps.initialSize())
                        .maxSize(poolProps.maxSize())
                        .maxIdleTime(Duration.ofMinutes(poolProps.maxIdleTime()))
                        .validationQuery("SELECT 1")
                        .build();

        return new ConnectionPool(poolConfiguration);
    }
}
