package co.franquicia.usecase.franquicia;

import co.franquicia.model.franquicia.Franquicia;
import co.franquicia.model.franquicia.gateways.FranquiciaRepository;
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
class FranquiciaUseCaseTest {

    @Mock
    private FranquiciaRepository repository;

    private FranquiciaUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FranquiciaUseCase(repository);
    }

    @Test
    void crearFranquicia_ok() {
        String nombre = "Nueva";
        Franquicia creada = Franquicia.builder().id("id-1").nombre(nombre).build();
        when(repository.crearFranquicia(nombre)).thenReturn(Mono.just(creada));

        StepVerifier.create(useCase.crearFranquicia(nombre))
                .expectNext(creada)
                .verifyComplete();
    }

    @Test
    void crearFranquicia_nombreInvalido() {
        StepVerifier.create(useCase.crearFranquicia("  "))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void obtenerPorId_notFound() {
        when(repository.obtenerPorId("id-404")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.obtenerPorId("id-404"))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void obtenerPorId_ok() {
        Franquicia f = Franquicia.builder().id("id-1").nombre("X").build();
        when(repository.obtenerPorId("id-1")).thenReturn(Mono.just(f));

        StepVerifier.create(useCase.obtenerPorId("id-1"))
                .expectNext(f)
                .verifyComplete();
    }

    @Test
    void obtenerTodas_ok() {
        when(repository.obtenerFranquicias()).thenReturn(Flux.just(
                Franquicia.builder().id("1").build(),
                Franquicia.builder().id("2").build()
        ));

        StepVerifier.create(useCase.obtenerTodas())
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void actualizarFranquicia_ok() {
        Franquicia cambios = Franquicia.builder().nombre("Nuevo").build();
        Franquicia actualizado = Franquicia.builder().id("id-1").nombre("Nuevo").build();
        when(repository.actualizarFranquicia("id-1", cambios)).thenReturn(Mono.just(actualizado));

        StepVerifier.create(useCase.actualizarFranquicia("id-1", cambios))
                .expectNext(actualizado)
                .verifyComplete();
    }

    @Test
    void eliminarPorId_ok() {
        when(repository.eliminarPorId("id-1")).thenReturn(Mono.just("ok"));

        StepVerifier.create(useCase.eliminarPorId("id-1"))
                .expectNext("ok")
                .verifyComplete();
    }

    @Test
    void contar_ok() {
        when(repository.contar()).thenReturn(Mono.just(5L));

        StepVerifier.create(useCase.contar())
                .expectNext(5L)
                .verifyComplete();
    }

    @Test
    void obtenerPorNombre_ok() {
        String nombre = "  Subway  ";
        Franquicia f = Franquicia.builder().id("id-1").nombre("Subway").build();

        when(repository.obtenerPorNombre("Subway")).thenReturn(Mono.just(f));

        StepVerifier.create(useCase.obtenerPorNombre(nombre))
                .expectNext(f)
                .verifyComplete();
    }

    @Test
    void obtenerPorNombre_nombreInvalido() {
        StepVerifier.create(useCase.obtenerPorNombre("   "))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void obtenerPorNombre_notFound() {
        when(repository.obtenerPorNombre("NoExiste")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.obtenerPorNombre("NoExiste"))
                .expectErrorMatches(e ->
                        e instanceof IllegalArgumentException &&
                                "Franquicia no encontrada".equals(e.getMessage())
                )
                .verify();
    }

    @Test
    void buscarPorNombreContaining_ok() {
        String nombre = "  bur  ";

        when(repository.buscarPorNombreContaining("bur"))
                .thenReturn(Flux.just(
                        Franquicia.builder().id("1").nombre("Burger King").build(),
                        Franquicia.builder().id("2").nombre("Burgerville").build()
                ));

        StepVerifier.create(useCase.buscarPorNombreContaining(nombre))
                .expectNextMatches(f -> f.getNombre() != null && f.getNombre().contains("Burg"))
                .expectNextMatches(f -> f.getNombre() != null && f.getNombre().contains("Burg"))
                .verifyComplete();
    }

    @Test
    void buscarPorNombreContaining_nombreInvalido() {
        StepVerifier.create(useCase.buscarPorNombreContaining("   "))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void buscarPorNombreContaining_sinResultados_ok() {
        when(repository.buscarPorNombreContaining("zzz")).thenReturn(Flux.empty());

        StepVerifier.create(useCase.buscarPorNombreContaining("zzz"))
                .verifyComplete();
    }

    @Test
    void eliminarPorNombre_ok() {
        String nombre = "  Subway  ";
        when(repository.eliminarPorNombre("Subway")).thenReturn(Mono.just("ok"));

        StepVerifier.create(useCase.eliminarPorNombre(nombre))
                .expectNext("ok")
                .verifyComplete();
    }

    @Test
    void eliminarPorNombre_nombreInvalido() {
        StepVerifier.create(useCase.eliminarPorNombre("   "))
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}
