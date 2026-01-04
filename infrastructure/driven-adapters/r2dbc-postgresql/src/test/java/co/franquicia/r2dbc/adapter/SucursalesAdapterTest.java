package co.franquicia.r2dbc.adapter;

import co.franquicia.model.sucursal.Sucursal;
import co.franquicia.r2dbc.entity.SucursalData;
import co.franquicia.r2dbc.repository.ReactiveSucursalesRepository;
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
class SucursalesAdapterTest {

    @Mock
    private ReactiveSucursalesRepository repository;

    private SucursalesAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new SucursalesAdapter(repository, new ObjectMapperImp());
    }

    @Test
    void obtenerPorId_convierteUUID() {
        UUID uuid = UUID.randomUUID();
        SucursalData data = SucursalData.builder()
                .id(uuid)
                .franquiciaId(UUID.randomUUID())
                .nombre("Centro")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        when(repository.findById(uuid)).thenReturn(Mono.just(data));

        StepVerifier.create(adapter.obtenerPorId(uuid.toString()))
                .assertNext(s -> assertThat(s.getId()).isEqualTo(uuid.toString()))
                .verifyComplete();

        ArgumentCaptor<UUID> captor = ArgumentCaptor.forClass(UUID.class);
        verify(repository).findById(captor.capture());
        assertThat(captor.getValue()).isEqualTo(uuid);
    }

    @Test
    void crearSucursal_convierteFkUUID() {
        UUID franquicia = UUID.randomUUID();
        when(repository.findByNombreAndFranquiciaId("Centro", franquicia))
                .thenReturn(Mono.empty());

        when(repository.save(any(SucursalData.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0, SucursalData.class)));

        StepVerifier.create(adapter.crearSucursal(franquicia.toString(), "Centro"))
                .assertNext(s -> {
                    assertThat(s.getFranquiciaId()).isEqualTo(franquicia.toString());
                    assertThat(s.getNombre()).isEqualTo("Centro");
                    assertThat(s.getCreatedAt()).isNotNull();
                    assertThat(s.getUpdatedAt()).isNotNull();
                })
                .verifyComplete();

        ArgumentCaptor<SucursalData> captor = ArgumentCaptor.forClass(SucursalData.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getFranquiciaId()).isEqualTo(franquicia);
        assertThat(captor.getValue().getNombre()).isEqualTo("Centro");
    }

    @Test
    void obtenerTodas_ok() {
        when(repository.findAll()).thenReturn(Flux.just(
                SucursalData.builder().id(UUID.randomUUID()).franquiciaId(UUID.randomUUID()).nombre("A").createdAt(Instant.now()).updatedAt(Instant.now()).build(),
                SucursalData.builder().id(UUID.randomUUID()).franquiciaId(UUID.randomUUID()).nombre("B").createdAt(Instant.now()).updatedAt(Instant.now()).build()
        ));

        StepVerifier.create(adapter.obtenerTodas())
                .expectNextCount(2)
                .verifyComplete();

        verify(repository).findAll();
    }

    @Test
    void obtenerPorFranquicia_convierteUUIDyMapea() {
        UUID franquiciaId = UUID.randomUUID();

        when(repository.findByFranquiciaId(franquiciaId)).thenReturn(Flux.just(
                SucursalData.builder().id(UUID.randomUUID()).franquiciaId(franquiciaId).nombre("Centro").createdAt(Instant.now()).updatedAt(Instant.now()).build(),
                SucursalData.builder().id(UUID.randomUUID()).franquiciaId(franquiciaId).nombre("Norte").createdAt(Instant.now()).updatedAt(Instant.now()).build()
        ));

        StepVerifier.create(adapter.obtenerPorFranquicia(franquiciaId.toString()))
                .assertNext(s -> assertThat(s.getFranquiciaId()).isEqualTo(franquiciaId.toString()))
                .assertNext(s -> assertThat(s.getFranquiciaId()).isEqualTo(franquiciaId.toString()))
                .verifyComplete();

        verify(repository).findByFranquiciaId(franquiciaId);
    }

    @Test
    void obtenerPorNombre_ok() {
        when(repository.findByNombre("Centro")).thenReturn(Flux.just(
                SucursalData.builder().id(UUID.randomUUID()).franquiciaId(UUID.randomUUID()).nombre("Centro").createdAt(Instant.now()).updatedAt(Instant.now()).build(),
                SucursalData.builder().id(UUID.randomUUID()).franquiciaId(UUID.randomUUID()).nombre("Centro").createdAt(Instant.now()).updatedAt(Instant.now()).build()
        ));

        StepVerifier.create(adapter.obtenerPorNombre("Centro"))
                .expectNextCount(2)
                .verifyComplete();

        verify(repository).findByNombre("Centro");
    }

    @Test
    void buscarPorNombreContaining_ok() {
        when(repository.findByNombreContaining("cen")).thenReturn(Flux.just(
                SucursalData.builder().id(UUID.randomUUID()).franquiciaId(UUID.randomUUID()).nombre("Centro").createdAt(Instant.now()).updatedAt(Instant.now()).build(),
                SucursalData.builder().id(UUID.randomUUID()).franquiciaId(UUID.randomUUID()).nombre("Central Park").createdAt(Instant.now()).updatedAt(Instant.now()).build()
        ));

        StepVerifier.create(adapter.buscarPorNombreContaining("cen"))
                .assertNext(s -> assertThat(s.getNombre().toLowerCase()).contains("cen"))
                .assertNext(s -> assertThat(s.getNombre().toLowerCase()).contains("cen"))
                .verifyComplete();

        verify(repository).findByNombreContaining("cen");
    }

    @Test
    void eliminarPorId_ok() {
        UUID id = UUID.randomUUID();
        when(repository.deleteById(id)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.eliminarPorId(id.toString()))
                .expectNext("Sucursal eliminada correctamente")
                .verifyComplete();

        verify(repository).deleteById(id);
    }

    @Test
    void actualizarSucursal_ok_actualizaNombreYUpdatedAt() {
        UUID sucursalId = UUID.randomUUID();
        Instant oldUpdatedAt = Instant.now().minusSeconds(500);

        SucursalData existente = SucursalData.builder()
                .id(sucursalId)
                .franquiciaId(UUID.randomUUID())
                .nombre("Viejo")
                .createdAt(Instant.now().minusSeconds(1000))
                .updatedAt(oldUpdatedAt)
                .build();

        when(repository.findById(sucursalId)).thenReturn(Mono.just(existente));
        when(repository.save(any(SucursalData.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0, SucursalData.class)));

        Sucursal cambios = Sucursal.builder().nombre("Nuevo").build();

        StepVerifier.create(adapter.actualizarSucursal(sucursalId.toString(), cambios))
                .assertNext(s -> {
                    assertThat(s.getId()).isEqualTo(sucursalId.toString());
                    assertThat(s.getNombre()).isEqualTo("Nuevo");
                    assertThat(s.getUpdatedAt()).isNotNull();
                })
                .verifyComplete();

        ArgumentCaptor<SucursalData> captor = ArgumentCaptor.forClass(SucursalData.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getNombre()).isEqualTo("Nuevo");
        assertThat(captor.getValue().getUpdatedAt()).isAfter(oldUpdatedAt);
    }

    @Test
    void actualizarSucursal_notFound_devuelveEmpty() {
        UUID sucursalId = UUID.randomUUID();
        when(repository.findById(sucursalId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.actualizarSucursal(sucursalId.toString(), Sucursal.builder().nombre("X").build()))
                .verifyComplete();

        verify(repository, never()).save(any());
    }

    @Test
    void actualizarSucursal_duplicateKey_mapeaAIllegalState() {
        UUID sucursalId = UUID.randomUUID();

        when(repository.findById(sucursalId)).thenReturn(Mono.just(
                SucursalData.builder()
                        .id(sucursalId)
                        .franquiciaId(UUID.randomUUID())
                        .nombre("Viejo")
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build()
        ));
        when(repository.save(any(SucursalData.class)))
                .thenReturn(Mono.error(new DuplicateKeyException("dup")));

        StepVerifier.create(adapter.actualizarSucursal(sucursalId.toString(), Sucursal.builder().nombre("Nuevo").build()))
                .expectErrorMatches(e ->
                        e instanceof IllegalStateException &&
                                e.getMessage() != null &&
                                e.getMessage().contains("El nombre de la sucursal ya existe en esta franquicia")
                )
                .verify();
    }

    @Test
    void contarPorFranquicia_ok() {
        UUID franquiciaId = UUID.randomUUID();
        when(repository.countByFranquiciaId(franquiciaId)).thenReturn(Mono.just(4L));

        StepVerifier.create(adapter.contarPorFranquicia(franquiciaId.toString()))
                .expectNext(4L)
                .verifyComplete();

        verify(repository).countByFranquiciaId(franquiciaId);
    }
}
