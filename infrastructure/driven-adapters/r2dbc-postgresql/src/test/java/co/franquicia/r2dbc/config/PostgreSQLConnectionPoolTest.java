package co.franquicia.r2dbc.config;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.spi.ConnectionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PostgreSQLConnectionPoolTest {

    @Test
    void buildsConnectionPoolAndTransactionManager() {
        PostgreSQLConnectionPool config = new PostgreSQLConnectionPool();
        PostgresqlConnectionProperties dbProps = new PostgresqlConnectionProperties(
                "localhost", 5432, "db", "public", "user", "pass"
        );
        R2dbcPoolProperties poolProps = new R2dbcPoolProperties(1, 2, 1);

        ConnectionFactory factory = config.connectionFactory(dbProps, poolProps);
        assertThat(factory).isInstanceOf(ConnectionPool.class);
        assertThat(((ConnectionPool) factory).getMetadata().getName()).isNotBlank();

        R2dbcTransactionManager txManager = config.transactionManager(factory);
        assertThat(txManager).isNotNull();
    }

    @Test
    void registersUuidStringConverters() {
        PostgreSQLConnectionPool config = new PostgreSQLConnectionPool();
        PostgresqlConnectionProperties dbProps = new PostgresqlConnectionProperties(
                "localhost", 5432, "db", "public", "user", "pass"
        );
        R2dbcPoolProperties poolProps = new R2dbcPoolProperties(1, 2, 1);
        ConnectionFactory factory = config.connectionFactory(dbProps, poolProps);

        DefaultConversionService registry = new DefaultConversionService();
        registry.addConverter(new R2dbcConverters.StringToUuidWritingConverter());
        registry.addConverter(new R2dbcConverters.UuidToStringReadingConverter());

        UUID uuid = UUID.randomUUID();
        assertThat(registry.convert(uuid.toString(), UUID.class)).isEqualTo(uuid);
        assertThat(registry.convert(uuid, String.class)).isEqualTo(uuid.toString());
    }
}
