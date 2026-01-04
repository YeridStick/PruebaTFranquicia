package co.franquicia.usecase.producto;

import co.franquicia.model.producto.Producto;
import co.franquicia.model.producto.gateways.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoUseCaseTest {

    @Mock
    private ProductoRepository repository;

    private ProductoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ProductoUseCase(repository);
    }

    @Test
    void crearProducto_ok() {
        Producto creado = Producto.builder()
                .id("p-1")
                .sucursalId("s-1")
                .nombre("Hamburguesa")
                .precio(1000)
                .stock(10)
                .build();

        when(repository.crearProducto("s-1", "Hamburguesa", 1000, 10))
                .thenReturn(Mono.just(creado));

        StepVerifier.create(useCase.crearProducto("s-1", "Hamburguesa", 1000, 10))
                .expectNext(creado)
                .verifyComplete();
    }

    @Test
    void crearProducto_precioInvalido() {
        StepVerifier.create(useCase.crearProducto("s-1", "X", 0, 1))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void obtenerPorNombre_variosResultados() {
        when(repository.obtenerPorNombre("Hamburguesa"))
                .thenReturn(Flux.just(
                        Producto.builder().id("p-1").nombre("Hamburguesa").build(),
                        Producto.builder().id("p-2").nombre("Hamburguesa").build()
                ));

        StepVerifier.create(useCase.obtenerPorNombre("Hamburguesa"))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void obtenerPorId_ok() {
        Producto p = Producto.builder().id("p-1").nombre("X").build();
        when(repository.obtenerPorId("p-1")).thenReturn(Mono.just(p));

        StepVerifier.create(useCase.obtenerPorId("p-1"))
                .expectNext(p)
                .verifyComplete();
    }

    @Test
    void obtenerTodos_ok() {
        when(repository.obtenerTodos()).thenReturn(Flux.just(
                Producto.builder().id("1").build(),
                Producto.builder().id("2").build()
        ));

        StepVerifier.create(useCase.obtenerTodos())
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void actualizarProducto_ok() {
        Producto cambios = Producto.builder().nombre("Nuevo").precio(10).stock(1).build();
        Producto actualizado = Producto.builder().id("p-1").nombre("Nuevo").precio(10).stock(1).build();
        when(repository.actualizarProducto("p-1", cambios)).thenReturn(Mono.just(actualizado));

        StepVerifier.create(useCase.actualizarProducto("p-1", cambios))
                .expectNext(actualizado)
                .verifyComplete();
    }

    @Test
    void eliminarPorId_ok() {
        when(repository.eliminarPorId("p-1")).thenReturn(Mono.just("ok"));

        StepVerifier.create(useCase.eliminarPorId("p-1"))
                .expectNext("ok")
                .verifyComplete();
    }

    @Test
    void obtenerPorSucursal_ok() {
        when(repository.obtenerPorSucursal("s-1"))
                .thenReturn(Flux.just(
                        Producto.builder().id("p-1").sucursalId("s-1").nombre("A").build(),
                        Producto.builder().id("p-2").sucursalId("s-1").nombre("B").build()
                ));

        StepVerifier.create(useCase.obtenerPorSucursal("s-1"))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void obtenerPorSucursal_idInvalido() {
        StepVerifier.create(useCase.obtenerPorSucursal("   "))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void obtenerPorSucursal_sinResultados_ok() {
        when(repository.obtenerPorSucursal("s-404")).thenReturn(Flux.empty());

        StepVerifier.create(useCase.obtenerPorSucursal("s-404"))
                .verifyComplete();
    }

    @Test
    void buscarPorNombreEnSucursal_ok() {
        when(repository.buscarPorNombreEnSucursal("s-1", "bur"))
                .thenReturn(Flux.just(
                        Producto.builder().id("p-1").sucursalId("s-1").nombre("Burger").build(),
                        Producto.builder().id("p-2").sucursalId("s-1").nombre("Burrito").build()
                ));

        StepVerifier.create(useCase.buscarPorNombreEnSucursal("s-1", "  bur  "))
                .expectNextMatches(p -> p.getNombre() != null && p.getNombre().toLowerCase().contains("bur"))
                .expectNextMatches(p -> p.getNombre() != null && p.getNombre().toLowerCase().contains("bur"))
                .verifyComplete();
    }

    @Test
    void buscarPorNombreEnSucursal_idInvalido() {
        StepVerifier.create(useCase.buscarPorNombreEnSucursal("   ", "bur"))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void buscarPorNombreEnSucursal_nombreInvalido() {
        StepVerifier.create(useCase.buscarPorNombreEnSucursal("s-1", "   "))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void buscarPorNombreEnSucursal_sinResultados_ok() {
        when(repository.buscarPorNombreEnSucursal("s-1", "zzz"))
                .thenReturn(Flux.empty());

        StepVerifier.create(useCase.buscarPorNombreEnSucursal("s-1", "zzz"))
                .verifyComplete();
    }

    @Test
    void buscarPorStockBajo_ok() {
        when(repository.buscarPorStockBajo(3))
                .thenReturn(Flux.just(
                        Producto.builder().id("p-1").stock(1).build(),
                        Producto.builder().id("p-2").stock(0).build()
                ));

        StepVerifier.create(useCase.buscarPorStockBajo(3))
                .expectNextMatches(p -> p.getStock() < 3)
                .expectNextMatches(p -> p.getStock() < 3)
                .verifyComplete();
    }

    @Test
    void buscarPorStockBajo_stockInvalido() {
        StepVerifier.create(useCase.buscarPorStockBajo(-1))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void buscarPorStockBajo_sinResultados_ok() {
        when(repository.buscarPorStockBajo(1)).thenReturn(Flux.empty());

        StepVerifier.create(useCase.buscarPorStockBajo(1))
                .verifyComplete();
    }

    @Test
    void obtenerMasCaro_ok() {
        Producto caro = Producto.builder().id("p-9").sucursalId("s-1").precio(9999).build();
        when(repository.obtenerMasCaro("s-1")).thenReturn(Mono.just(caro));

        StepVerifier.create(useCase.obtenerMasCaro("s-1"))
                .expectNext(caro)
                .verifyComplete();
    }

    @Test
    void obtenerMasCaro_idInvalido() {
        StepVerifier.create(useCase.obtenerMasCaro("   "))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void obtenerMasCaro_sinProductos_error() {
        when(repository.obtenerMasCaro("s-1")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.obtenerMasCaro("s-1"))
                .expectErrorMatches(e ->
                        e instanceof IllegalArgumentException &&
                                "No hay productos en esta sucursal".equals(e.getMessage())
                )
                .verify();
    }

    @Test
    void contarPorSucursal_ok() {
        when(repository.contarPorSucursal("s-1")).thenReturn(Mono.just(7L));

        StepVerifier.create(useCase.contarPorSucursal("s-1"))
                .expectNext(7L)
                .verifyComplete();
    }

    @Test
    void contarPorSucursal_idInvalido() {
        StepVerifier.create(useCase.contarPorSucursal("   "))
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}
