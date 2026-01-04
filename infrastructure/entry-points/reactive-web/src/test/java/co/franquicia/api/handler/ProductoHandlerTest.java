package co.franquicia.api.handler;

import co.franquicia.api.router.RouterRest;
import co.franquicia.model.producto.Producto;
import co.franquicia.usecase.producto.ProductoUseCase;
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
class ProductoHandlerTest {

    @Mock
    private ProductoUseCase productoUseCase;
    @Mock
    private FranquiciaHandler franquiciaHandler;
    @Mock
    private SucursalHandler sucursalHandler;

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        ProductoHandler handler = new ProductoHandler(productoUseCase);
        RouterRest router = new RouterRest();
        client = WebTestClient.bindToRouterFunction(
                router.routerFunction(franquiciaHandler, sucursalHandler, handler)
        ).build();
    }

    @Test
    void obtenerTodosDevuelveLista() {
        when(productoUseCase.obtenerTodos()).thenReturn(Flux.just(
                Producto.builder().id("p1").nombre("Prod").build()
        ));

        client.get()
                .uri("/api/productos")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].nombre").isEqualTo("Prod");
    }

    @Test
    void obtenerPorNombreDevuelveLista() {
        when(productoUseCase.obtenerPorNombre("Prod")).thenReturn(Flux.just(
                Producto.builder().id("p1").nombre("Prod").build()
        ));

        client.get()
                .uri("/api/productos/nombre/Prod")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1);
    }

    @Test
    void crearProductoDevuelveCreado() {
        when(productoUseCase.crearProducto(any(), any(), any(Long.class), any(Integer.class)))
                .thenReturn(Mono.just(Producto.builder().id("p1").nombre("Nuevo").build()));

        client.post()
                .uri("/api/sucursales/s1/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"Nuevo\",\"precio\":10,\"stock\":1}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("p1");
    }

    @Test
    void obtenerPorId_ok() {
        when(productoUseCase.obtenerPorId("p1"))
                .thenReturn(Mono.just(Producto.builder().id("p1").nombre("Prod").build()));

        client.get()
                .uri("/api/productos/p1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo("p1")
                .jsonPath("$.nombre").isEqualTo("Prod");
    }

    @Test
    void obtenerPorId_notFound_cuandoIllegalArgument() {
        when(productoUseCase.obtenerPorId("p404"))
                .thenReturn(Mono.error(new IllegalArgumentException("Producto no encontrado")));

        client.get()
                .uri("/api/productos/p404")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void obtenerPorId_error500() {
        when(productoUseCase.obtenerPorId("p1"))
                .thenReturn(Mono.error(new RuntimeException("boom")));

        client.get()
                .uri("/api/productos/p1")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class).isEqualTo("Error al obtener producto");
    }

    @Test
    void obtenerPorSucursal_ok() {
        when(productoUseCase.obtenerPorSucursal("s1")).thenReturn(Flux.just(
                Producto.builder().id("p1").sucursalId("s1").nombre("A").build(),
                Producto.builder().id("p2").sucursalId("s1").nombre("B").build()
        ));

        client.get()
                .uri("/api/sucursales/s1/productos")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].sucursalId").isEqualTo("s1");
    }

    @Test
    void obtenerPorSucursal_badRequest_cuandoIllegalArgument() {
        when(productoUseCase.obtenerPorSucursal("s1"))
                .thenReturn(Flux.error(new IllegalArgumentException("ID de sucursal obligatorio")));

        client.get()
                .uri("/api/sucursales/s1/productos")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class).isEqualTo("Error: ID de sucursal obligatorio");
    }

    @Test
    void obtenerPorSucursal_error500() {
        when(productoUseCase.obtenerPorSucursal("s1"))
                .thenReturn(Flux.error(new RuntimeException("boom")));

        client.get()
                .uri("/api/sucursales/s1/productos")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class).isEqualTo("Error al obtener productos");
    }

    @Test
    void buscarPorNombreEnSucursal_ok() {
        when(productoUseCase.buscarPorNombreEnSucursal("s1", "bur")).thenReturn(Flux.just(
                Producto.builder().id("p1").sucursalId("s1").nombre("Burger").build(),
                Producto.builder().id("p2").sucursalId("s1").nombre("Burrito").build()
        ));

        client.get()
                .uri("/api/sucursales/s1/productos/buscar/bur")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].nombre").exists();
    }

    @Test
    void buscarPorNombreEnSucursal_badRequest_cuandoIllegalArgument() {
        when(productoUseCase.buscarPorNombreEnSucursal("s1", "x"))
                .thenReturn(Flux.error(new IllegalArgumentException("El nombre es obligatorio")));

        client.get()
                .uri("/api/sucursales/s1/productos/buscar/x")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class).isEqualTo("Error: El nombre es obligatorio");
    }

    @Test
    void buscarPorNombreEnSucursal_error500() {
        when(productoUseCase.buscarPorNombreEnSucursal("s1", "x"))
                .thenReturn(Flux.error(new RuntimeException("boom")));

        client.get()
                .uri("/api/sucursales/s1/productos/buscar/x")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class).isEqualTo("Error al buscar productos");
    }

    @Test
    void buscarPorStockBajo_ok() {
        when(productoUseCase.buscarPorStockBajo(3)).thenReturn(Flux.just(
                Producto.builder().id("p1").stock(1).build(),
                Producto.builder().id("p2").stock(2).build()
        ));

        client.get()
                .uri("/api/productos/stock-bajo/3")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2);
    }

    @Test
    void buscarPorStockBajo_badRequest_siStockNoEsNumero() {
        client.get()
                .uri("/api/productos/stock-bajo/abc")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class).isEqualTo("Stock debe ser un número válido");
    }

    @Test
    void buscarPorStockBajo_error500_siUseCaseFalla() {
        when(productoUseCase.buscarPorStockBajo(3))
                .thenReturn(Flux.error(new RuntimeException("boom")));

        client.get()
                .uri("/api/productos/stock-bajo/3")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class).isEqualTo("Error al buscar productos");
    }

    @Test
    void obtenerMasCaro_ok() {
        when(productoUseCase.obtenerMasCaro("s1"))
                .thenReturn(Mono.just(Producto.builder().id("p9").sucursalId("s1").precio(999).build()));

        client.get()
                .uri("/api/sucursales/s1/productos/mas-caro")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo("p9")
                .jsonPath("$.sucursalId").isEqualTo("s1");
    }

    @Test
    void obtenerMasCaro_badRequest_cuandoIllegalArgument() {
        when(productoUseCase.obtenerMasCaro("s1"))
                .thenReturn(Mono.error(new IllegalArgumentException("No hay productos")));

        client.get()
                .uri("/api/sucursales/s1/productos/mas-caro")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class).isEqualTo("Error: No hay productos");
    }

    @Test
    void obtenerMasCaro_error500() {
        when(productoUseCase.obtenerMasCaro("s1"))
                .thenReturn(Mono.error(new RuntimeException("boom")));

        client.get()
                .uri("/api/sucursales/s1/productos/mas-caro")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class).isEqualTo("Error al obtener producto");
    }

    @Test
    void actualizarProducto_ok() {
        when(productoUseCase.actualizarProducto(eq("p1"), any(Producto.class)))
                .thenReturn(Mono.just(Producto.builder().id("p1").nombre("Nuevo").build()));

        client.put()
                .uri("/api/productos/p1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"Nuevo\",\"precio\":10,\"stock\":1}")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo("p1")
                .jsonPath("$.nombre").isEqualTo("Nuevo");
    }

    @Test
    void actualizarProducto_badRequest_cuandoIllegalArgument() {
        when(productoUseCase.actualizarProducto(eq("p1"), any(Producto.class)))
                .thenReturn(Mono.error(new IllegalArgumentException("dato invalido")));

        client.put()
                .uri("/api/productos/p1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"\"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class).isEqualTo("Error: dato invalido");
    }

    @Test
    void actualizarProducto_error500() {
        when(productoUseCase.actualizarProducto(eq("p1"), any(Producto.class)))
                .thenReturn(Mono.error(new RuntimeException("boom")));

        client.put()
                .uri("/api/productos/p1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"Nuevo\",\"precio\":10,\"stock\":1}")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class).isEqualTo("Error al actualizar producto");
    }

    @Test
    void eliminarPorId_ok() {
        when(productoUseCase.eliminarPorId("p1")).thenReturn(Mono.just("ok"));

        client.delete()
                .uri("/api/productos/p1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("ok");
    }

    @Test
    void eliminarPorId_badRequest_cuandoIllegalArgument() {
        when(productoUseCase.eliminarPorId("p1"))
                .thenReturn(Mono.error(new IllegalArgumentException("id invalido")));

        client.delete()
                .uri("/api/productos/p1")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class).isEqualTo("Error: id invalido");
    }

    @Test
    void eliminarPorId_error500() {
        when(productoUseCase.eliminarPorId("p1"))
                .thenReturn(Mono.error(new RuntimeException("boom")));

        client.delete()
                .uri("/api/productos/p1")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class).isEqualTo("Error al eliminar producto");
    }

    @Test
    void contarPorSucursal_ok() {
        when(productoUseCase.contarPorSucursal("s1")).thenReturn(Mono.just(5L));

        client.get()
                .uri("/api/sucursales/s1/productos/contar")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Long.class).isEqualTo(5L);
    }

    @Test
    void contarPorSucursal_badRequest_cuandoIllegalArgument() {
        when(productoUseCase.contarPorSucursal("s1"))
                .thenReturn(Mono.error(new IllegalArgumentException("ID de sucursal obligatorio")));

        client.get()
                .uri("/api/sucursales/s1/productos/contar")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class).isEqualTo("Error: ID de sucursal obligatorio");
    }

    @Test
    void contarPorSucursal_error500() {
        when(productoUseCase.contarPorSucursal("s1"))
                .thenReturn(Mono.error(new RuntimeException("boom")));

        client.get()
                .uri("/api/sucursales/s1/productos/contar")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class).isEqualTo("Error al contar productos");
    }
}
