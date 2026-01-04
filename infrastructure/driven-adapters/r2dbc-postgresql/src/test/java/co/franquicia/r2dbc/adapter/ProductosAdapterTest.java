package co.franquicia.r2dbc.adapter;

import co.franquicia.model.producto.Producto;
import co.franquicia.r2dbc.entity.ProductoData;
import co.franquicia.r2dbc.repository.ReactiveProductosRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapperImp;
import org.springframework.dao.DuplicateKeyException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductosAdapterTest {

    @Mock
    private ReactiveProductosRepository repository;

    private ProductosAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ProductosAdapter(repository, new ObjectMapperImp());
    }

    @Test
    void obtenerPorId_convierteUUID() {
        UUID uuid = UUID.randomUUID();
        ProductoData data = ProductoData.builder()
                .id(uuid)
                .sucursalId(UUID.randomUUID())
                .nombre("Prod")
                .precio(10)
                .stock(1)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        when(repository.findById(uuid)).thenReturn(Mono.just(data));

        StepVerifier.create(adapter.obtenerPorId(uuid.toString()))
                .assertNext(p -> assertThat(p.getId()).isEqualTo(uuid.toString()))
                .verifyComplete();

        ArgumentCaptor<UUID> captor = ArgumentCaptor.forClass(UUID.class);
        verify(repository).findById(captor.capture());
        assertThat(captor.getValue()).isEqualTo(uuid);
    }

    @Test
    void crearProducto_convierteFkUUID() {
        UUID sucursal = UUID.randomUUID();
        when(repository.findByNombre("Prod")).thenReturn(Flux.empty());

        when(repository.save(any(ProductoData.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0, ProductoData.class)));

        StepVerifier.create(adapter.crearProducto(sucursal.toString(), "Prod", 10, 1))
                .assertNext(p -> {
                    assertThat(p.getSucursalId()).isEqualTo(sucursal.toString());
                    assertThat(p.getNombre()).isEqualTo("Prod");
                    assertThat(p.getPrecio()).isEqualTo(10);
                    assertThat(p.getStock()).isEqualTo(1);
                    assertThat(p.getCreatedAt()).isNotNull();
                    assertThat(p.getUpdatedAt()).isNotNull();
                })
                .verifyComplete();

        ArgumentCaptor<ProductoData> captor = ArgumentCaptor.forClass(ProductoData.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getSucursalId()).isEqualTo(sucursal);
        assertThat(captor.getValue().getNombre()).isEqualTo("Prod");
    }

    @Test
    void obtenerTodos_ok() {
        when(repository.findAll()).thenReturn(Flux.just(
                ProductoData.builder().id(UUID.randomUUID()).sucursalId(UUID.randomUUID()).nombre("A").precio(1).stock(1).createdAt(Instant.now()).updatedAt(Instant.now()).build(),
                ProductoData.builder().id(UUID.randomUUID()).sucursalId(UUID.randomUUID()).nombre("B").precio(2).stock(2).createdAt(Instant.now()).updatedAt(Instant.now()).build()
        ));

        StepVerifier.create(adapter.obtenerTodos())
                .expectNextCount(2)
                .verifyComplete();

        verify(repository).findAll();
    }

    @Test
    void obtenerPorSucursal_convierteUUIDyMapea() {
        UUID sucursalId = UUID.randomUUID();

        when(repository.findBySucursalId(sucursalId)).thenReturn(Flux.just(
                ProductoData.builder().id(UUID.randomUUID()).sucursalId(sucursalId).nombre("A").precio(1).stock(1).createdAt(Instant.now()).updatedAt(Instant.now()).build(),
                ProductoData.builder().id(UUID.randomUUID()).sucursalId(sucursalId).nombre("B").precio(2).stock(2).createdAt(Instant.now()).updatedAt(Instant.now()).build()
        ));

        StepVerifier.create(adapter.obtenerPorSucursal(sucursalId.toString()))
                .assertNext(p -> assertThat(p.getSucursalId()).isEqualTo(sucursalId.toString()))
                .assertNext(p -> assertThat(p.getSucursalId()).isEqualTo(sucursalId.toString()))
                .verifyComplete();

        verify(repository).findBySucursalId(sucursalId);
    }

    @Test
    void obtenerPorNombre_ok() {
        when(repository.findByNombre("Prod")).thenReturn(Flux.just(
                ProductoData.builder().id(UUID.randomUUID()).sucursalId(UUID.randomUUID()).nombre("Prod").precio(10).stock(1).createdAt(Instant.now()).updatedAt(Instant.now()).build(),
                ProductoData.builder().id(UUID.randomUUID()).sucursalId(UUID.randomUUID()).nombre("Prod").precio(20).stock(2).createdAt(Instant.now()).updatedAt(Instant.now()).build()
        ));

        StepVerifier.create(adapter.obtenerPorNombre("Prod"))
                .expectNextCount(2)
                .verifyComplete();

        verify(repository).findByNombre("Prod");
    }

    @Test
    void buscarPorNombreEnSucursal_convierteUUIDyUsaContaining() {
        UUID sucursalId = UUID.randomUUID();

        when(repository.findBySucursalIdAndNombreContaining(sucursalId, "bur")).thenReturn(Flux.just(
                ProductoData.builder().id(UUID.randomUUID()).sucursalId(sucursalId).nombre("Burger").precio(10).stock(1).createdAt(Instant.now()).updatedAt(Instant.now()).build(),
                ProductoData.builder().id(UUID.randomUUID()).sucursalId(sucursalId).nombre("Burrito").precio(12).stock(2).createdAt(Instant.now()).updatedAt(Instant.now()).build()
        ));

        StepVerifier.create(adapter.buscarPorNombreEnSucursal(sucursalId.toString(), "bur"))
                .assertNext(p -> {
                    assertThat(p.getSucursalId()).isEqualTo(sucursalId.toString());
                    assertThat(p.getNombre().toLowerCase()).contains("bur");
                })
                .assertNext(p -> {
                    assertThat(p.getSucursalId()).isEqualTo(sucursalId.toString());
                    assertThat(p.getNombre().toLowerCase()).contains("bur");
                })
                .verifyComplete();

        verify(repository).findBySucursalIdAndNombreContaining(sucursalId, "bur");
    }

    @Test
    void buscarPorStockBajo_ok() {
        when(repository.findByStockLessThan(3)).thenReturn(Flux.just(
                ProductoData.builder().id(UUID.randomUUID()).sucursalId(UUID.randomUUID()).nombre("A").precio(1).stock(1).createdAt(Instant.now()).updatedAt(Instant.now()).build(),
                ProductoData.builder().id(UUID.randomUUID()).sucursalId(UUID.randomUUID()).nombre("B").precio(2).stock(2).createdAt(Instant.now()).updatedAt(Instant.now()).build()
        ));

        StepVerifier.create(adapter.buscarPorStockBajo(3))
                .assertNext(p -> assertThat(p.getStock()).isLessThan(3))
                .assertNext(p -> assertThat(p.getStock()).isLessThan(3))
                .verifyComplete();

        verify(repository).findByStockLessThan(3);
    }

    @Test
    void obtenerMasCaro_ok_convierteUUID() {
        UUID sucursalId = UUID.randomUUID();
        ProductoData caro = ProductoData.builder()
                .id(UUID.randomUUID())
                .sucursalId(sucursalId)
                .nombre("Caro")
                .precio(999)
                .stock(1)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(repository.findMostExpensiveInSucursal(sucursalId)).thenReturn(Mono.just(caro));

        StepVerifier.create(adapter.obtenerMasCaro(sucursalId.toString()))
                .assertNext(p -> {
                    assertThat(p.getSucursalId()).isEqualTo(sucursalId.toString());
                    assertThat(p.getNombre()).isEqualTo("Caro");
                    assertThat(p.getPrecio()).isEqualTo(999);
                })
                .verifyComplete();

        verify(repository).findMostExpensiveInSucursal(sucursalId);
    }

    @Test
    void eliminarPorId_ok() {
        UUID id = UUID.randomUUID();
        when(repository.deleteById(id)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.eliminarPorId(id.toString()))
                .expectNext("Producto eliminado correctamente")
                .verifyComplete();

        verify(repository).deleteById(id);
    }

    @Test
    void actualizarProducto_ok_actualizaCamposYUpdatedAt() {
        UUID productoId = UUID.randomUUID();
        Instant oldUpdatedAt = Instant.now().minusSeconds(500);

        ProductoData existente = ProductoData.builder()
                .id(productoId)
                .sucursalId(UUID.randomUUID())
                .nombre("Viejo")
                .precio(1)
                .stock(1)
                .createdAt(Instant.now().minusSeconds(1000))
                .updatedAt(oldUpdatedAt)
                .build();

        when(repository.findById(productoId)).thenReturn(Mono.just(existente));
        when(repository.save(any(ProductoData.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0, ProductoData.class)));

        Producto cambios = Producto.builder()
                .nombre("Nuevo")
                .precio(10)
                .stock(5)
                .build();

        StepVerifier.create(adapter.actualizarProducto(productoId.toString(), cambios))
                .assertNext(p -> {
                    assertThat(p.getId()).isEqualTo(productoId.toString());
                    assertThat(p.getNombre()).isEqualTo("Nuevo");
                    assertThat(p.getPrecio()).isEqualTo(10);
                    assertThat(p.getStock()).isEqualTo(5);
                    assertThat(p.getUpdatedAt()).isNotNull();
                })
                .verifyComplete();

        ArgumentCaptor<ProductoData> captor = ArgumentCaptor.forClass(ProductoData.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getNombre()).isEqualTo("Nuevo");
        assertThat(captor.getValue().getPrecio()).isEqualTo(10);
        assertThat(captor.getValue().getStock()).isEqualTo(5);
        assertThat(captor.getValue().getUpdatedAt()).isAfter(oldUpdatedAt);
    }

    @Test
    void actualizarProducto_notFound_devuelveEmpty() {
        UUID productoId = UUID.randomUUID();
        when(repository.findById(productoId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.actualizarProducto(productoId.toString(),
                        Producto.builder().nombre("X").precio(1).stock(1).build()))
                .verifyComplete();

        verify(repository, never()).save(any());
    }

    @Test
    void actualizarProducto_duplicateKey_mapeaAIllegalState() {
        UUID productoId = UUID.randomUUID();
        when(repository.findById(productoId)).thenReturn(Mono.just(
                ProductoData.builder()
                        .id(productoId)
                        .sucursalId(UUID.randomUUID())
                        .nombre("Viejo")
                        .precio(1)
                        .stock(1)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build()
        ));
        when(repository.save(any(ProductoData.class)))
                .thenReturn(Mono.error(new DuplicateKeyException("dup")));

        StepVerifier.create(adapter.actualizarProducto(productoId.toString(),
                        Producto.builder().nombre("Nuevo").precio(2).stock(2).build()))
                .expectErrorMatches(e ->
                        e instanceof IllegalStateException &&
                                e.getMessage() != null &&
                                e.getMessage().contains("El nombre del producto ya existe")
                )
                .verify();
    }

    @Test
    void contarPorSucursal_ok() {
        UUID sucursalId = UUID.randomUUID();
        when(repository.countBySucursalId(sucursalId)).thenReturn(Mono.just(7L));

        StepVerifier.create(adapter.contarPorSucursal(sucursalId.toString()))
                .expectNext(7L)
                .verifyComplete();

        verify(repository).countBySucursalId(sucursalId);
    }
}
