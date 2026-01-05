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

@Repository
public class SucursalesAdapter extends ReactiveAdapterOperations<
        Sucursal,
        SucursalData,
        java.util.UUID,
        ReactiveSucursalesRepository
> implements SucursalRepository {

    public SucursalesAdapter(ReactiveSucursalesRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> Sucursal.builder()
                .id(d.getId() != null ? d.getId().toString() : null)
                .franquiciaId(d.getFranquiciaId() != null ? d.getFranquiciaId().toString() : null)
                .nombre(d.getNombre())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build());
    }

    /**
     * Crea una nueva sucursal para una franquicia
     * @param franquiciaId id de la franquicia
     * @param nombre nombre de la sucursal
     * @return Mono con la sucursal creada
     */
    @Override
    public Mono<Sucursal> crearSucursal(String franquiciaId, String nombre) {
        java.util.UUID franquiciaUuid = java.util.UUID.fromString(franquiciaId);
        return repository.findByNombreAndFranquiciaId(nombre, franquiciaUuid)
                .flatMap(existing -> Mono.<SucursalData>error(new IllegalStateException("Sucursal ya existe en esta franquicia")))
                .switchIfEmpty(Mono.defer(() -> {
                    var data = SucursalData.builder()
                            .franquiciaId(franquiciaUuid)
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
        return repository.findById(java.util.UUID.fromString(id))
                .map(this::toEntity);
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
        return repository.findByFranquiciaId(java.util.UUID.fromString(franquiciaId))
                .map(this::toEntity);
    }

    /**
     * Obtiene una sucursal por nombre
     * @param nombre nombre de la sucursal
     * @return Mono con la sucursal encontrada
     */
    @Override
    public Flux<Sucursal> obtenerPorNombre(String nombre) {
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
        return repository.deleteById(java.util.UUID.fromString(id))
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
        return repository.findById(java.util.UUID.fromString(sucursalId))
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
        return repository.countByFranquiciaId(java.util.UUID.fromString(franquiciaId));
    }

    /**
     * Cuenta todas las sucursales
     * @return Mono con el total
     */
    @Override
    public Mono<Long> contarTodas() {
        return repository.countAll();
    }
}
