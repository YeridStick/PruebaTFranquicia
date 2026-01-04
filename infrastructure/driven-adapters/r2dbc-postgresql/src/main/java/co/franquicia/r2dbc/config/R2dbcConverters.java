package co.franquicia.r2dbc.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import java.util.UUID;

/**
 * Converters to bridge UUID columns with String ids in the domain layer.
 */
public final class R2dbcConverters {

    private R2dbcConverters() {
    }

    @WritingConverter
    public static class StringToUuidWritingConverter implements Converter<String, UUID> {
        @Override
        public UUID convert(String source) {
            return source == null ? null : UUID.fromString(source);
        }
    }

    @ReadingConverter
    public static class UuidToStringReadingConverter implements Converter<UUID, String> {
        @Override
        public String convert(UUID source) {
            return source == null ? null : source.toString();
        }
    }
}
