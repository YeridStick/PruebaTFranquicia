package co.franquicia.usecase.sucursal;

import co.franquicia.model.sucursal.Sucursal;
import co.franquicia.model.sucursal.gateways.SucursalRepository;
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
class SucursalUseCaseTest {

    @Mock
    private SucursalRepository repository;

    private SucursalUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SucursalUseCase(repository);
    }

    @Test
    void crearSucursal_ok() {
        String franquiciaId = "f-1";
        String nombre = "Centro";
        Sucursal creada = Sucursal.builder().id("s-1").franquiciaId(franquiciaId).nombre(nombre).build();
        when(repository.crearSucursal(franquiciaId, nombre)).thenReturn(Mono.just(creada));

        StepVerifier.create(useCase.crearSucursal(franquiciaId, nombre))
                .expectNext(creada)
                .verifyComplete();
    }

    @Test
    void obtenerPorNombre_variosResultados() {
        String nombre = "Centro";
        when(repository.obtenerPorNombre(nombre))
                .thenReturn(Flux.just(
                        Sucursal.builder().id("s-1").nombre(nombre).build(),
                        Sucursal.builder().id("s-2").nombre(nombre).build()
                ));

        StepVerifier.create(useCase.obtenerPorNombre(nombre))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void obtenerPorNombre_invalido() {
        StepVerifier.create(useCase.obtenerPorNombre("  "))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void obtenerPorId_ok() {
        Sucursal s = Sucursal.builder().id("s-1").nombre("Centro").build();
        when(repository.obtenerPorId("s-1")).thenReturn(Mono.just(s));

        StepVerifier.create(useCase.obtenerPorId("s-1"))
                .expectNext(s)
                .verifyComplete();
    }

    @Test
    void obtenerTodas_ok() {
        when(repository.obtenerTodas()).thenReturn(Flux.just(
                Sucursal.builder().id("1").build(),
                Sucursal.builder().id("2").build()
        ));

        StepVerifier.create(useCase.obtenerTodas())
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void actualizarSucursal_ok() {
        Sucursal cambios = Sucursal.builder().nombre("Nuevo").build();
        Sucursal actualizado = Sucursal.builder().id("s-1").nombre("Nuevo").build();
        when(repository.actualizarSucursal("s-1", cambios)).thenReturn(Mono.just(actualizado));

        StepVerifier.create(useCase.actualizarSucursal("s-1", cambios))
                .expectNext(actualizado)
                .verifyComplete();
    }

    @Test
    void eliminarPorId_ok() {
        when(repository.eliminarPorId("s-1")).thenReturn(Mono.just("ok"));

        StepVerifier.create(useCase.eliminarPorId("s-1"))
                .expectNext("ok")
                .verifyComplete();
    }

    @Test
    void contarPorFranquicia_ok() {
        when(repository.contarPorFranquicia("f-1")).thenReturn(Mono.just(3L));

        StepVerifier.create(useCase.contarPorFranquicia("f-1"))
                .expectNext(3L)
                .verifyComplete();
    }
}
