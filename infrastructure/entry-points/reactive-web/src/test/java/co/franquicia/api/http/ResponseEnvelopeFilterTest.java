package co.franquicia.api.http;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.WebHandler;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseEnvelopeFilterTest {

    private WebTestClient client(WebHandler handler) {
        return WebTestClient.bindToWebHandler(handler)
                .webFilter(new ResponseEnvelopeFilter())
                .build();
    }

    @Test
    void wrapsPlainJson2xxResponse() {
        WebHandler handler = exchange -> {
            var res = exchange.getResponse();
            res.setStatusCode(HttpStatus.OK);
            res.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            var buf = res.bufferFactory().wrap("{\"foo\":1}".getBytes());
            return res.writeWith(Mono.just(buf));
        };

        client(handler).get().uri("/api/test").exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    assertThat(body).contains("\"status\":200");
                    assertThat(body).contains("\"message\":\"OK\"");
                    assertThat(body).contains("\"data\":{\"foo\":1}");
                });
    }

    @Test
    void doesNotWrapIfAlreadyEnveloped() {
        String original = "{\"status\":200,\"message\":\"OK\",\"data\":{\"foo\":1}}";
        WebHandler handler = exchange -> {
            var res = exchange.getResponse();
            res.setStatusCode(HttpStatus.OK);
            res.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            var buf = res.bufferFactory().wrap(original.getBytes());
            return res.writeWith(Mono.just(buf));
        };

        client(handler).get().uri("/api/test").exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo(original);
    }

    @Test
    void skipsWhenHeaderPresent() {
        WebHandler handler = exchange -> {
            var res = exchange.getResponse();
            res.setStatusCode(HttpStatus.OK);
            res.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            var buf = res.bufferFactory().wrap("{\"foo\":1}".getBytes());
            return res.writeWith(Mono.just(buf));
        };

        client(handler).mutate().defaultHeaders(h -> h.add("X-Envelope-Skip", "true")).build()
                .get().uri("/api/test").exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("{\"foo\":1}");
    }

    @Test
    void doesNotWrapNonJson() {
        WebHandler handler = exchange -> {
            var res = exchange.getResponse();
            res.setStatusCode(HttpStatus.OK);
            res.getHeaders().setContentType(MediaType.TEXT_PLAIN);
            var buf = res.bufferFactory().wrap("plain".getBytes());
            return res.writeWith(Mono.just(buf));
        };

        client(handler).get().uri("/api/test").exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("plain");
    }

    @Test
    void doesNotWrapNon2xx() {
        WebHandler handler = exchange -> {
            var res = exchange.getResponse();
            res.setStatusCode(HttpStatus.BAD_REQUEST);
            res.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            var buf = res.bufferFactory().wrap("{\"error\":\"x\"}".getBytes());
            return res.writeWith(Mono.just(buf));
        };

        client(handler).get().uri("/api/test").exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .isEqualTo("{\"error\":\"x\"}");
    }
}
