package co.franquicia.api.handler;

import co.franquicia.api.router.RouterRest;
import co.franquicia.model.franquicia.Franquicia;
import co.franquicia.usecase.franquicia.FranquiciaUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FranquiciaHandlerTest {

    @Mock
    private FranquiciaUseCase franquiciaUseCase;
    @Mock
    private SucursalHandler sucursalHandler;
    @Mock
    private ProductoHandler productoHandler;

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        FranquiciaHandler handler = new FranquiciaHandler(franquiciaUseCase);
        RouterRest router = new RouterRest();
        client = WebTestClient.bindToRouterFunction(
                router.routerFunction(handler, sucursalHandler, productoHandler)
        ).build();
    }

    @Test
    void obtenerTodasDevuelveListado() {
        when(franquiciaUseCase.obtenerTodas()).thenReturn(Flux.just(
                Franquicia.builder().id("1").nombre("A").build(),
                Franquicia.builder().id("2").nombre("B").build()
        ));

        client.get()
                .uri("/api/franquicias")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].nombre").isEqualTo("A");
    }

    @Test
    void obtenerPorIdDevuelveFranquicia() {
        when(franquiciaUseCase.obtenerPorId("1"))
                .thenReturn(Mono.just(Franquicia.builder().id("1").nombre("A").build()));

        client.get()
                .uri("/api/franquicias/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("1")
                .jsonPath("$.nombre").isEqualTo("A");
    }

    @Test
    void crearFranquiciaDevuelveCreada() {
        Franquicia creada = Franquicia.builder().id("1").nombre("Nueva").build();
        when(franquiciaUseCase.crearFranquicia(anyString())).thenReturn(Mono.just(creada));

        client.post()
                .uri("/api/franquicias")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"Nueva\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("1")
                .jsonPath("$.nombre").isEqualTo("Nueva");
    }

    @Test
    void obtenerPorNombre_ok() {
        when(franquiciaUseCase.obtenerPorNombre("Subway"))
                .thenReturn(Mono.just(Franquicia.builder().id("10").nombre("Subway").build()));

        client.get()
                .uri("/api/franquicias/nombre/Subway")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo("10")
                .jsonPath("$.nombre").isEqualTo("Subway");
    }

    @Test
    void obtenerPorNombre_notFound_cuandoUseCaseLanzaIllegalArgument() {
        when(franquiciaUseCase.obtenerPorNombre("NoExiste"))
                .thenReturn(Mono.error(new IllegalArgumentException("Franquicia no encontrada")));

        client.get()
                .uri("/api/franquicias/nombre/NoExiste")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void obtenerPorNombre_error500_cuandoErrorNoControlado() {
        when(franquiciaUseCase.obtenerPorNombre("X"))
                .thenReturn(Mono.error(new RuntimeException("boom")));

        client.get()
                .uri("/api/franquicias/nombre/X")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class).isEqualTo("Error al obtener franquicia");
    }

    @Test
    void buscarPorNombreContaining_ok_devuelveLista() {
        when(franquiciaUseCase.buscarPorNombreContaining("bur"))
                .thenReturn(Flux.just(
                        Franquicia.builder().id("1").nombre("Burger King").build(),
                        Franquicia.builder().id("2").nombre("Burgerville").build()
                ));

        client.get()
                .uri("/api/franquicias/buscar/bur")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].nombre").exists();
    }

    @Test
    void buscarPorNombreContaining_error500() {
        when(franquiciaUseCase.buscarPorNombreContaining("x"))
                .thenReturn(Flux.error(new RuntimeException("boom")));

        client.get()
                .uri("/api/franquicias/buscar/x")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class).isEqualTo("Error al buscar franquicias");
    }

    @Test
    void actualizarFranquicia_ok() {
        Franquicia actualizado = Franquicia.builder().id("1").nombre("Nuevo").build();
        when(franquiciaUseCase.actualizarFranquicia(anyString(), any(Franquicia.class)))
                .thenReturn(Mono.just(actualizado));

        client.put()
                .uri("/api/franquicias/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"Nuevo\"}")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo("1")
                .jsonPath("$.nombre").isEqualTo("Nuevo");
    }

    @Test
    void actualizarFranquicia_badRequest_cuandoIllegalArgument() {
        when(franquiciaUseCase.actualizarFranquicia(anyString(), any(Franquicia.class)))
                .thenReturn(Mono.error(new IllegalArgumentException("dato invalido")));

        client.put()
                .uri("/api/franquicias/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"\"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class).isEqualTo("Error: dato invalido");
    }

    @Test
    void actualizarFranquicia_error500() {
        when(franquiciaUseCase.actualizarFranquicia(anyString(), any(Franquicia.class)))
                .thenReturn(Mono.error(new RuntimeException("boom")));

        client.put()
                .uri("/api/franquicias/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"Nuevo\"}")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class).isEqualTo("Error al actualizar franquicia");
    }

    @Test
    void eliminarPorId_ok() {
        when(franquiciaUseCase.eliminarPorId("1")).thenReturn(Mono.just("ok"));

        client.delete()
                .uri("/api/franquicias/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("ok");
    }

    @Test
    void eliminarPorId_badRequest_cuandoIllegalArgument() {
        when(franquiciaUseCase.eliminarPorId("1"))
                .thenReturn(Mono.error(new IllegalArgumentException("id invalido")));

        client.delete()
                .uri("/api/franquicias/1")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class).isEqualTo("Error: id invalido");
    }

    @Test
    void eliminarPorId_error500() {
        when(franquiciaUseCase.eliminarPorId("1"))
                .thenReturn(Mono.error(new RuntimeException("boom")));

        client.delete()
                .uri("/api/franquicias/1")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class).isEqualTo("Error al eliminar franquicia");
    }

    @Test
    void eliminarPorNombre_ok() {
        when(franquiciaUseCase.eliminarPorNombre("Subway")).thenReturn(Mono.just("ok"));

        client.delete()
                .uri("/api/franquicias/nombre/Subway")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("ok");
    }

    @Test
    void eliminarPorNombre_badRequest_cuandoIllegalArgument() {
        when(franquiciaUseCase.eliminarPorNombre("X"))
                .thenReturn(Mono.error(new IllegalArgumentException("nombre invalido")));

        client.delete()
                .uri("/api/franquicias/nombre/X")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class).isEqualTo("Error: nombre invalido");
    }

    @Test
    void eliminarPorNombre_error500() {
        when(franquiciaUseCase.eliminarPorNombre("X"))
                .thenReturn(Mono.error(new RuntimeException("boom")));

        client.delete()
                .uri("/api/franquicias/nombre/X")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class).isEqualTo("Error al eliminar franquicia");
    }

    @Test
    void contar_ok() {
        when(franquiciaUseCase.contar()).thenReturn(Mono.just(5L));

        client.get()
                .uri("/api/franquicias/contar")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Long.class).isEqualTo(5L);
    }

    @Test
    void contar_error500() {
        when(franquiciaUseCase.contar()).thenReturn(Mono.error(new RuntimeException("boom")));

        client.get()
                .uri("/api/franquicias/contar")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class).isEqualTo("Error al contar franquicias");
    }
}
