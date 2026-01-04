package co.franquicia.api.error;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalErrorHandlerTest {

    private GlobalErrorHandler handler;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        handler = new GlobalErrorHandler(mapper);
    }

    @Test
    void handlesIllegalArgumentExceptionAsBadRequest() throws Exception {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/test").build());

        handler.handle(exchange, new IllegalArgumentException("bad input")).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exchange.getResponse().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        JsonNode json = bodyAsJson((MockServerHttpResponse) exchange.getResponse());
        assertThat(json.get("status").asInt()).isEqualTo(400);
        assertThat(json.get("message").asText()).contains("bad input");
        assertThat(isDataNull(json)).isTrue();
    }

    @Test
    void handlesNoSuchElementExceptionAsNotFound() throws Exception {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/test").build());

        handler.handle(exchange, new NoSuchElementException("missing")).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exchange.getResponse().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        JsonNode json = bodyAsJson((MockServerHttpResponse) exchange.getResponse());
        assertThat(json.get("status").asInt()).isEqualTo(404);
        assertThat(json.get("message").asText()).contains("missing");
        assertThat(isDataNull(json)).isTrue();
    }

    @Test
    void handlesIllegalStateExceptionWithNotFoundKeywordsAsNotFound() throws Exception {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/test").build());

        handler.handle(exchange, new IllegalStateException("Franquicia no encontrada")).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        JsonNode json = bodyAsJson((MockServerHttpResponse) exchange.getResponse());
        assertThat(json.get("status").asInt()).isEqualTo(404);
        assertThat(json.get("message").asText()).contains("no encontrada");
        assertThat(isDataNull(json)).isTrue();
    }

    @Test
    void handlesDuplicateKeyAsConflict() throws Exception {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/test").build());

        handler.handle(exchange, new DuplicateKeyException("dup")).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        JsonNode json = bodyAsJson((MockServerHttpResponse) exchange.getResponse());
        assertThat(json.get("status").asInt()).isEqualTo(409);
        assertThat(json.get("message").asText()).contains("dup");
        assertThat(isDataNull(json)).isTrue();
    }

    @Test
    void handlesDataIntegrityViolationAsConflict() throws Exception {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/test").build());

        handler.handle(exchange, new DataIntegrityViolationException("fk")).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        JsonNode json = bodyAsJson((MockServerHttpResponse) exchange.getResponse());
        assertThat(json.get("status").asInt()).isEqualTo(409);
        assertThat(json.get("message").asText()).contains("fk");
        assertThat(isDataNull(json)).isTrue();
    }

    @Test
    void handlesServerWebInputExceptionAsBadRequestWithGenericMessage() throws Exception {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/test").build());

        handler.handle(exchange, new ServerWebInputException("invalid")).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        JsonNode json = bodyAsJson((MockServerHttpResponse) exchange.getResponse());
        assertThat(json.get("status").asInt()).isEqualTo(400);
        // Tu handler mapea ServerWebInputException a "Solicitud inválida"
        assertThat(json.get("message").asText()).isEqualTo("Solicitud inválida");
        assertThat(isDataNull(json)).isTrue();
    }

    @Test
    void handlesGenericExceptionAsInternalServerError() throws Exception {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/test").build());

        handler.handle(exchange, new RuntimeException("boom")).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(exchange.getResponse().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        JsonNode json = bodyAsJson((MockServerHttpResponse) exchange.getResponse());
        assertThat(json.get("status").asInt()).isEqualTo(500);
        assertThat(json.get("message").asText()).contains("boom");
        assertThat(isDataNull(json)).isTrue();
    }

    // -------------------------
    // Helpers
    // -------------------------

    private JsonNode bodyAsJson(MockServerHttpResponse response) throws Exception {
        String body = bodyAsString(response);
        assertThat(body).isNotBlank();
        return mapper.readTree(body);
    }

    private String bodyAsString(MockServerHttpResponse response) {
        String body = response.getBodyAsString().block();
        return body == null ? "" : body;
    }

    private boolean isDataNull(JsonNode json) {
        JsonNode data = json.get("data");
        return data == null || data.isNull();
    }
}
