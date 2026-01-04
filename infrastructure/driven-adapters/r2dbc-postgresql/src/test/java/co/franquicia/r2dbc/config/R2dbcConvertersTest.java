package co.franquicia.r2dbc.config;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class R2dbcConvertersTest {

    @Test
    void stringToUuid() {
        UUID uuid = UUID.randomUUID();
        UUID converted = new R2dbcConverters.StringToUuidWritingConverter().convert(uuid.toString());
        assertThat(converted).isEqualTo(uuid);
    }

    @Test
    void uuidToString() {
        UUID uuid = UUID.randomUUID();
        String converted = new R2dbcConverters.UuidToStringReadingConverter().convert(uuid);
        assertThat(converted).isEqualTo(uuid.toString());
    }
}
