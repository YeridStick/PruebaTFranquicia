package co.franquicia.api.handler;

import co.franquicia.api.router.RouterRest;
import co.franquicia.model.sucursal.Sucursal;
import co.franquicia.usecase.sucursal.SucursalUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SucursalHandlerTest {

    @Mock
    private SucursalUseCase sucursalUseCase;
    @Mock
    private FranquiciaHandler franquiciaHandler;
    @Mock
    private ProductoHandler productoHandler;

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        SucursalHandler handler = new SucursalHandler(sucursalUseCase);
        RouterRest router = new RouterRest();
        client = WebTestClient.bindToRouterFunction(
                router.routerFunction(franquiciaHandler, handler, productoHandler)
        ).build();
    }

    @Test
    void obtenerTodasDevuelveListado() {
        when(sucursalUseCase.obtenerTodas()).thenReturn(Flux.just(
                Sucursal.builder().id("s1").nombre("Centro").build()
        ));

        client.get()
                .uri("/api/sucursales")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].nombre").isEqualTo("Centro");
    }

    @Test
    void obtenerPorNombreDevuelveLista() {
        when(sucursalUseCase.obtenerPorNombre("Centro")).thenReturn(Flux.just(
                Sucursal.builder().id("s1").nombre("Centro").build(),
                Sucursal.builder().id("s2").nombre("Centro").build()
        ));

        client.get()
                .uri("/api/sucursales/nombre/Centro")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2);
    }

    @Test
    void crearSucursalDevuelveCreada() {
        when(sucursalUseCase.crearSucursal(anyString(), anyString()))
                .thenReturn(Mono.just(Sucursal.builder().id("s1").nombre("Nueva").build()));

        client.post()
                .uri("/api/franquicias/f1/sucursales")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"Nueva\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("s1");
    }

    @Test
    void obtenerTodas_error500() {
        when(sucursalUseCase.obtenerTodas()).thenReturn(Flux.error(new RuntimeException("boom")));

        client.get()
                .uri("/api/sucursales")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class).isEqualTo("Error al obtener sucursales");
    }

    @Test
    void obtenerPorId_ok() {
        when(sucursalUseCase.obtenerPorId("s1"))
                .thenReturn(Mono.just(Sucursal.builder().id("s1").nombre("Centro").build()));

        client.get()
                .uri("/api/sucursales/s1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo("s1")
                .jsonPath("$.nombre").isEqualTo("Centro");
    }

    @Test
    void obtenerPorId_notFound_cuandoIllegalArgument() {
        when(sucursalUseCase.obtenerPorId("s404"))
                .thenReturn(Mono.error(new IllegalArgumentException("Sucursal no encontrada")));

        client.get()
                .uri("/api/sucursales/s404")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void obtenerPorId_error500() {
        when(sucursalUseCase.obtenerPorId("s1"))
                .thenReturn(Mono.error(new RuntimeException("boom")));

        client.get()
                .uri("/api/sucursales/s1")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class).isEqualTo("Error al obtener sucursal");
    }

    @Test
    void obtenerPorNombre_ok_devuelveLista() {
        when(sucursalUseCase.obtenerPorNombre("Centro")).thenReturn(Flux.just(
                Sucursal.builder().id("s1").nombre("Centro").build(),
                Sucursal.builder().id("s2").nombre("Centro").build()
        ));

        client.get()
                .uri("/api/sucursales/nombre/Centro")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2);
    }

    @Test
    void obtenerPorNombre_error500() {
        when(sucursalUseCase.obtenerPorNombre("Centro"))
                .thenReturn(Flux.error(new RuntimeException("boom")));

        client.get()
                .uri("/api/sucursales/nombre/Centro")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class).isEqualTo("Error al obtener sucursal");
    }

    @Test
    void obtenerPorFranquicia_ok() {
        when(sucursalUseCase.obtenerPorFranquicia("f1")).thenReturn(Flux.just(
                Sucursal.builder().id("s1").franquiciaId("f1").nombre("Centro").build(),
                Sucursal.builder().id("s2").franquiciaId("f1").nombre("Norte").build()
        ));

        client.get()
                .uri("/api/franquicias/f1/sucursales")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].franquiciaId").isEqualTo("f1");
    }

    @Test
    void obtenerPorFranquicia_badRequest_cuandoIllegalArgument() {
        when(sucursalUseCase.obtenerPorFranquicia("f1"))
                .thenReturn(Flux.error(new IllegalArgumentException("ID de franquicia obligatorio")));

        client.get()
                .uri("/api/franquicias/f1/sucursales")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class).isEqualTo("Error: ID de franquicia obligatorio");
    }

    @Test
    void obtenerPorFranquicia_error500() {
        when(sucursalUseCase.obtenerPorFranquicia("f1"))
                .thenReturn(Flux.error(new RuntimeException("boom")));

        client.get()
                .uri("/api/franquicias/f1/sucursales")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class).isEqualTo("Error al obtener sucursales");
    }

    @Test
    void buscarPorNombreContaining_ok() {
        when(sucursalUseCase.buscarPorNombreContaining("cen")).thenReturn(Flux.just(
                Sucursal.builder().id("s1").nombre("Centro").build(),
                Sucursal.builder().id("s2").nombre("Central").build()
        ));

        client.get()
                .uri("/api/sucursales/buscar/cen")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2);
    }

    @Test
    void buscarPorNombreContaining_error500() {
        when(sucursalUseCase.buscarPorNombreContaining("cen"))
                .thenReturn(Flux.error(new RuntimeException("boom")));

        client.get()
                .uri("/api/sucursales/buscar/cen")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class).isEqualTo("Error al buscar sucursales");
    }

    @Test
    void crearSucursal_badRequest_cuandoIllegalArgument() {
        when(sucursalUseCase.crearSucursal(eq("f1"), anyString()))
                .thenReturn(Mono.error(new IllegalArgumentException("El nombre es obligatorio")));

        client.post()
                .uri("/api/franquicias/f1/sucursales")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"\"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class).isEqualTo("Error: El nombre es obligatorio");
    }

    @Test
    void crearSucursal_error500() {
        when(sucursalUseCase.crearSucursal(eq("f1"), anyString()))
                .thenReturn(Mono.error(new RuntimeException("boom")));

        client.post()
                .uri("/api/franquicias/f1/sucursales")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"Nueva\"}")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class).isEqualTo("Error al crear sucursal");
    }

    @Test
    void actualizarSucursal_ok() {
        when(sucursalUseCase.actualizarSucursal(eq("s1"), any(Sucursal.class)))
                .thenReturn(Mono.just(Sucursal.builder().id("s1").nombre("Nuevo").build()));

        client.put()
                .uri("/api/sucursales/s1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"Nuevo\"}")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo("s1")
                .jsonPath("$.nombre").isEqualTo("Nuevo");
    }

    @Test
    void actualizarSucursal_badRequest_cuandoIllegalArgument() {
        when(sucursalUseCase.actualizarSucursal(eq("s1"), any(Sucursal.class)))
                .thenReturn(Mono.error(new IllegalArgumentException("dato invalido")));

        client.put()
                .uri("/api/sucursales/s1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"\"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class).isEqualTo("Error: dato invalido");
    }

    @Test
    void actualizarSucursal_error500() {
        when(sucursalUseCase.actualizarSucursal(eq("s1"), any(Sucursal.class)))
                .thenReturn(Mono.error(new RuntimeException("boom")));

        client.put()
                .uri("/api/sucursales/s1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"Nuevo\"}")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class).isEqualTo("Error al actualizar sucursal");
    }

    @Test
    void eliminarPorId_ok() {
        when(sucursalUseCase.eliminarPorId("s1")).thenReturn(Mono.just("ok"));

        client.delete()
                .uri("/api/sucursales/s1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("ok");
    }

    @Test
    void eliminarPorId_badRequest_cuandoIllegalArgument() {
        when(sucursalUseCase.eliminarPorId("s1"))
                .thenReturn(Mono.error(new IllegalArgumentException("id invalido")));

        client.delete()
                .uri("/api/sucursales/s1")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class).isEqualTo("Error: id invalido");
    }

    @Test
    void eliminarPorId_error500() {
        when(sucursalUseCase.eliminarPorId("s1"))
                .thenReturn(Mono.error(new RuntimeException("boom")));

        client.delete()
                .uri("/api/sucursales/s1")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class).isEqualTo("Error al eliminar sucursal");
    }

    @Test
    void contarPorFranquicia_ok() {
        when(sucursalUseCase.contarPorFranquicia("f1")).thenReturn(Mono.just(3L));

        client.get()
                .uri("/api/franquicias/f1/sucursales/contar")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Long.class).isEqualTo(3L);
    }

    @Test
    void contarPorFranquicia_badRequest_cuandoIllegalArgument() {
        when(sucursalUseCase.contarPorFranquicia("f1"))
                .thenReturn(Mono.error(new IllegalArgumentException("ID de franquicia obligatorio")));

        client.get()
                .uri("/api/franquicias/f1/sucursales/contar")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class).isEqualTo("Error: ID de franquicia obligatorio");
    }

    @Test
    void contarPorFranquicia_error500() {
        when(sucursalUseCase.contarPorFranquicia("f1"))
                .thenReturn(Mono.error(new RuntimeException("boom")));

        client.get()
                .uri("/api/franquicias/f1/sucursales/contar")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class).isEqualTo("Error al contar sucursales");
    }
}
