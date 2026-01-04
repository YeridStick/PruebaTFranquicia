package co.franquicia.r2dbc.adapter;

import co.franquicia.model.franquicia.Franquicia;
import co.franquicia.r2dbc.entity.FranquiciaData;
import co.franquicia.r2dbc.repository.ReactiveFranquiciaRepository;
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
class FranquiciasAdapterTest {

    @Mock
    private ReactiveFranquiciaRepository repository;

    private FranquiciasAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new FranquiciasAdapter(repository, new ObjectMapperImp());
    }

    @Test
    void obtenerPorId_convierteUUID() {
        UUID uuid = UUID.randomUUID();
        FranquiciaData data = FranquiciaData.builder()
                .id(uuid)
                .nombre("Test")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        when(repository.findById(uuid)).thenReturn(Mono.just(data));

        StepVerifier.create(adapter.obtenerPorId(uuid.toString()))
                .assertNext(f -> {
                    assertThat(f.getId()).isEqualTo(uuid.toString());
                    assertThat(f.getNombre()).isEqualTo("Test");
                })
                .verifyComplete();

        ArgumentCaptor<UUID> captor = ArgumentCaptor.forClass(UUID.class);
        verify(repository).findById(captor.capture());
        assertThat(captor.getValue()).isEqualTo(uuid);
    }

    // =========================
    // crearFranquicia(...)
    // =========================

    @Test
    void crearFranquicia_insertaCuandoNoExisteNombre() {
        String nombre = "Nueva";
        when(repository.findByNombre(nombre)).thenReturn(Mono.empty());
        when(repository.save(any(FranquiciaData.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0, FranquiciaData.class)));

        StepVerifier.create(adapter.crearFranquicia(nombre))
                .assertNext(f -> {
                    assertThat(f.getId()).isNull(); // porque en el mock devolvemos el mismo objeto sin id
                    assertThat(f.getNombre()).isEqualTo(nombre);
                    assertThat(f.getCreatedAt()).isNotNull();
                    assertThat(f.getUpdatedAt()).isNotNull();
                })
                .verifyComplete();

        // Verifica que guardó con nombre correcto
        ArgumentCaptor<FranquiciaData> captor = ArgumentCaptor.forClass(FranquiciaData.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getNombre()).isEqualTo(nombre);
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
        assertThat(captor.getValue().getUpdatedAt()).isNotNull();
    }

    @Test
    void crearFranquicia_errorSiYaExiste() {
        String nombre = "Existente";
        when(repository.findByNombre(nombre))
                .thenReturn(Mono.just(FranquiciaData.builder().id(UUID.randomUUID()).nombre(nombre).build()));

        StepVerifier.create(adapter.crearFranquicia(nombre))
                .expectErrorMatches(e ->
                        e instanceof IllegalStateException &&
                                "Franquicia ya existe".equals(e.getMessage())
                )
                .verify();

        verify(repository, never()).save(any());
    }

    @Test
    void crearFranquicia_duplicateKey_mapeaAIllegalState() {
        String nombre = "Duplicada";
        when(repository.findByNombre(nombre)).thenReturn(Mono.empty());
        when(repository.save(any(FranquiciaData.class)))
                .thenReturn(Mono.error(new DuplicateKeyException("dup")));

        StepVerifier.create(adapter.crearFranquicia(nombre))
                .expectErrorMatches(e ->
                        e instanceof IllegalStateException &&
                                e.getMessage() != null &&
                                e.getMessage().contains("El nombre de la franquicia ya existe")
                )
                .verify();
    }

    // =========================
    // obtenerFranquicias()
    // =========================

    @Test
    void obtenerFranquicias_ok() {
        when(repository.findAll()).thenReturn(Flux.just(
                FranquiciaData.builder().id(UUID.randomUUID()).nombre("A").createdAt(Instant.now()).updatedAt(Instant.now()).build(),
                FranquiciaData.builder().id(UUID.randomUUID()).nombre("B").createdAt(Instant.now()).updatedAt(Instant.now()).build()
        ));

        StepVerifier.create(adapter.obtenerFranquicias())
                .expectNextCount(2)
                .verifyComplete();

        verify(repository).findAll();
    }

    // =========================
    // obtenerPorNombre(...)
    // =========================

    @Test
    void obtenerPorNombre_ok() {
        String nombre = "KFC";
        UUID id = UUID.randomUUID();
        when(repository.findByNombre(nombre)).thenReturn(Mono.just(
                FranquiciaData.builder().id(id).nombre(nombre).createdAt(Instant.now()).updatedAt(Instant.now()).build()
        ));

        StepVerifier.create(adapter.obtenerPorNombre(nombre))
                .assertNext(f -> {
                    assertThat(f.getId()).isEqualTo(id.toString());
                    assertThat(f.getNombre()).isEqualTo(nombre);
                })
                .verifyComplete();

        verify(repository).findByNombre(nombre);
    }

    @Test
    void obtenerPorNombre_notFound_devuelveEmpty() {
        when(repository.findByNombre("X")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.obtenerPorNombre("X"))
                .verifyComplete();
    }

    // =========================
    // buscarPorNombreContaining(...)
    // =========================

    @Test
    void buscarPorNombreContaining_ok() {
        when(repository.findByNombreContaining("bur")).thenReturn(Flux.just(
                FranquiciaData.builder().id(UUID.randomUUID()).nombre("Burger King").createdAt(Instant.now()).updatedAt(Instant.now()).build(),
                FranquiciaData.builder().id(UUID.randomUUID()).nombre("Burgerville").createdAt(Instant.now()).updatedAt(Instant.now()).build()
        ));

        StepVerifier.create(adapter.buscarPorNombreContaining("bur"))
                .expectNextCount(2)
                .verifyComplete();

        verify(repository).findByNombreContaining("bur");
    }

    @Test
    void buscarPorNombreContaining_sinResultados_ok() {
        when(repository.findByNombreContaining("zzz")).thenReturn(Flux.empty());

        StepVerifier.create(adapter.buscarPorNombreContaining("zzz"))
                .verifyComplete();
    }

    // =========================
    // eliminarPorId(...)
    // =========================

    @Test
    void eliminarPorId_ok() {
        UUID id = UUID.randomUUID();
        when(repository.deleteById(id)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.eliminarPorId(id.toString()))
                .expectNext("Franquicia eliminada correctamente")
                .verifyComplete();

        verify(repository).deleteById(id);
    }

    // =========================
    // eliminarPorNombre(...)
    // =========================

    @Test
    void eliminarPorNombre_ok() {
        String nombre = "Subway";
        // deleteByNombre puede retornar Mono<Void> o Mono<Long> según tu repo;
        // en ambos casos el .thenReturn(...) funcionará si devolvemos Mono.empty()
        when(repository.deleteByNombre(nombre)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.eliminarPorNombre(nombre))
                .expectNext("Franquicia eliminada correctamente")
                .verifyComplete();

        verify(repository).deleteByNombre(nombre);
    }

    // =========================
    // actualizarFranquicia(...)
    // =========================

    @Test
    void actualizarFranquicia_ok_actualizaNombreYUpdatedAt() {
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.now().minusSeconds(1000);
        Instant oldUpdatedAt = Instant.now().minusSeconds(500);

        FranquiciaData existente = FranquiciaData.builder()
                .id(id)
                .nombre("Viejo")
                .createdAt(createdAt)
                .updatedAt(oldUpdatedAt)
                .build();

        when(repository.findById(id)).thenReturn(Mono.just(existente));
        when(repository.save(any(FranquiciaData.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0, FranquiciaData.class)));

        Franquicia cambios = Franquicia.builder().nombre("Nuevo").build();

        StepVerifier.create(adapter.actualizarFranquicia(id.toString(), cambios))
                .assertNext(f -> {
                    assertThat(f.getId()).isEqualTo(id.toString());
                    assertThat(f.getNombre()).isEqualTo("Nuevo");
                    assertThat(f.getCreatedAt()).isEqualTo(createdAt);
                    assertThat(f.getUpdatedAt()).isNotNull();
                })
                .verifyComplete();

        ArgumentCaptor<FranquiciaData> captor = ArgumentCaptor.forClass(FranquiciaData.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getNombre()).isEqualTo("Nuevo");
        assertThat(captor.getValue().getUpdatedAt()).isAfter(oldUpdatedAt);
    }

    @Test
    void actualizarFranquicia_notFound_devuelveEmpty() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.actualizarFranquicia(id.toString(), Franquicia.builder().nombre("X").build()))
                .verifyComplete();

        verify(repository, never()).save(any());
    }

    @Test
    void actualizarFranquicia_duplicateKey_mapeaAIllegalState() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Mono.just(
                FranquiciaData.builder().id(id).nombre("Viejo").createdAt(Instant.now()).updatedAt(Instant.now()).build()
        ));
        when(repository.save(any(FranquiciaData.class)))
                .thenReturn(Mono.error(new DuplicateKeyException("dup")));

        StepVerifier.create(adapter.actualizarFranquicia(id.toString(), Franquicia.builder().nombre("Nuevo").build()))
                .expectErrorMatches(e ->
                        e instanceof IllegalStateException &&
                                e.getMessage() != null &&
                                e.getMessage().contains("El nombre de la franquicia ya existe")
                )
                .verify();
    }

    // =========================
    // contar()
    // =========================

    @Test
    void contar_ok() {
        when(repository.countAll()).thenReturn(Mono.just(3L));

        StepVerifier.create(adapter.contar())
                .expectNext(3L)
                .verifyComplete();

        verify(repository).countAll();
    }
}
