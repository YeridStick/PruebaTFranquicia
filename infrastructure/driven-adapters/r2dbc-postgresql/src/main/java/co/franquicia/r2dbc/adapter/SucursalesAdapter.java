package co.franquicia.r2dbc.adapter;

import co.franquicia.model.sucursal.Sucursal;
import co.franquicia.model.sucursal.gateways.SucursalRepository;
import co.franquicia.r2dbc.entity.SucursalData;
import co.franquicia.r2dbc.helper.ReactiveAdapterOperations;
import co.franquicia.r2dbc.repository.ReactiveSucursalesRepository;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Repository
public class SucursalesAdapter extends ReactiveAdapterOperations<
        Sucursal,
                SucursalData,
                String,
                ReactiveSucursalesRepository
> implements SucursalRepository {

    public SucursalesAdapter(ReactiveSucursalesRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, Sucursal.class));
    }

    /**
     * Crea una nueva sucursal para una franquicia
     * @param franquiciaId id de la franquicia
     * @param nombre nombre de la sucursal
     * @return Mono con la sucursal creada
     */
    @Override
    public Mono<Sucursal> crearSucursal(String franquiciaId, String nombre) {
        return repository.findByNombreAndFranquiciaId(nombre, franquiciaId)
                .flatMap(existing -> Mono.<SucursalData>error(new IllegalStateException("Sucursal ya existe en esta franquicia")))
                .switchIfEmpty(Mono.defer(() -> {
                    var data = SucursalData.builder()
                            .id(UUID.randomUUID().toString())
                            .franquiciaId(franquiciaId)
                            .nombre(nombre)
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build();
                    return repository.save(data);
                }))
                .map(this::toEntity)
                .onErrorMap(DuplicateKeyException.class,
                        e -> new IllegalStateException("El nombre de la sucursal ya existe en esta franquicia", e));
    }

    /**
     * Obtiene una sucursal por su ID
     * @param id id de la sucursal
     * @return Mono con la sucursal encontrada
     */
    @Override
    public Mono<Sucursal> obtenerPorId(String id) {
        return findById(id);
    }

    /**
     * Obtiene todas las sucursales
     * @return Flux con todas las sucursales
     */
    @Override
    public Flux<Sucursal> obtenerTodas() {
        return findAll();
    }

    /**
     * Obtiene todas las sucursales de una franquicia
     * @param franquiciaId id de la franquicia
     * @return Flux con las sucursales de la franquicia
     */
    @Override
    public Flux<Sucursal> obtenerPorFranquicia(String franquiciaId) {
        return repository.findByFranquiciaId(franquiciaId)
                .map(this::toEntity);
    }

    /**
     * Obtiene una sucursal por nombre
     * @param nombre nombre de la sucursal
     * @return Mono con la sucursal encontrada
     */
    @Override
    public Mono<Sucursal> obtenerPorNombre(String nombre) {
        return repository.findByNombre(nombre)
                .map(this::toEntity);
    }

    /**
     * Busca sucursales que contengan el nombre especificado
     * @param nombre parte del nombre a buscar
     * @return Flux con las sucursales encontradas
     */
    @Override
    public Flux<Sucursal> buscarPorNombreContaining(String nombre) {
        return repository.findByNombreContaining(nombre)
                .map(this::toEntity);
    }

    /**
     * Elimina una sucursal por su ID
     * @param id id de la sucursal
     * @return Mono con un mensaje de confirmación
     */
    @Override
    public Mono<String> eliminarPorId(String id) {
        return repository.deleteById(id)
                .thenReturn("Sucursal eliminada correctamente");
    }

    /**
     * Actualiza una sucursal
     * @param sucursalId id de la sucursal a actualizar
     * @param cambios cambios a aplicar
     * @return Mono con la sucursal actualizada
     */
    @Override
    public Mono<Sucursal> actualizarSucursal(String sucursalId, Sucursal cambios) {
        return repository.findById(sucursalId)
                .flatMap(existente -> {
                    existente.setNombre(cambios.getNombre());
                    existente.setUpdatedAt(Instant.now());
                    return repository.save(existente);
                })
                .map(this::toEntity)
                .onErrorMap(DuplicateKeyException.class,
                        e -> new IllegalStateException("El nombre de la sucursal ya existe en esta franquicia", e));
    }

    /**
     * Cuenta sucursales por franquicia
     * @param franquiciaId id de la franquicia
     * @return Mono con el total
     */
    @Override
    public Mono<Long> contarPorFranquicia(String franquiciaId) {
        return repository.countByFranquiciaId(franquiciaId);
    }
}