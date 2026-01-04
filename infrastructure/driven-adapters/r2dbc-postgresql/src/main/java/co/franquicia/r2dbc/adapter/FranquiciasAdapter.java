package co.franquicia.r2dbc.adapter;

import co.franquicia.model.franquicia.Franquicia;
import co.franquicia.model.franquicia.gateways.FranquiciaRepository;
import co.franquicia.r2dbc.entity.FranquiciaData;
import co.franquicia.r2dbc.helper.ReactiveAdapterOperations;
import co.franquicia.r2dbc.repository.ReactiveFranquiciaRepository;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Repository
@Slf4j
public class FranquiciasAdapter extends ReactiveAdapterOperations<
        Franquicia/* change for domain model */,
        FranquiciaData/* change for adapter model */,
        java.util.UUID,
        ReactiveFranquiciaRepository
> implements FranquiciaRepository {

    public FranquiciasAdapter(ReactiveFranquiciaRepository repository, ObjectMapper mapper) {
        /**
         *  Could be use mapper.mapBuilder if your domain model implement builder pattern
         *  super(repository, mapper, d -> mapper.mapBuilder(d,ObjectModel.ObjectModelBuilder.class).build());
         *  Or using mapper.map with the class of the object model
         */
        super(repository, mapper, d -> Franquicia.builder()
                .id(d.getId() != null ? d.getId().toString() : null)
                .nombre(d.getNombre())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build());
    }

    /**
     * Crea una nueva franquicia
     * @param nombre nombre de la franquicia
     * @return Mono con la franquicia creada
     */
    @Override
    public Mono<Franquicia> crearFranquicia(String nombre) {
        return repository.findByNombre(nombre)
                .flatMap(existing -> Mono.<FranquiciaData>error(new IllegalStateException("Franquicia ya existe")))
                .switchIfEmpty(Mono.defer(() -> {
                    var data = FranquiciaData.builder()
                            .nombre(nombre)
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build();
                    return repository.save(data);
                }))
                .map(this::toEntity)
                .onErrorMap(DuplicateKeyException.class,
                        e -> new IllegalStateException("El nombre de la franquicia ya existe", e));
    }

    /**
     * Obtiene una franquicia por su ID
     * @param id id de la franquicia
     * @return Mono con la franquicia encontrada
     */
    @Override
    public Mono<Franquicia> obtenerPorId(String id) {
        return repository.findById(java.util.UUID.fromString(id))
                .map(this::toEntity);
    }

    /**
     * Obtiene todas las franquicias
     * @return Flux con todas las franquicias
     */
    @Override
    public Flux<Franquicia> obtenerFranquicias() {
        return findAll();
    }

    /**
     * Obtiene una franquicia por nombre
     * @param nombre nombre de la franquicia
     * @return Mono con la franquicia encontrada
     */
    @Override
    public Mono<Franquicia> obtenerPorNombre(String nombre) {
        return repository.findByNombre(nombre)
                .map(this::toEntity);
    }

    /**
     * Busca franquicias que contengan el nombre especificado
     * @param nombre parte del nombre a buscar
     * @return Flux con las franquicias encontradas
     */
    @Override
    public Flux<Franquicia> buscarPorNombreContaining(String nombre) {
        return repository.findByNombreContaining(nombre)
                .map(this::toEntity);
    }

    /**
     * Elimina una franquicia por su ID
     * @param id id de la franquicia
     * @return Mono con un mensaje de confirmación
     */
    @Override
    public Mono<String> eliminarPorId(String id) {
        return repository.deleteById(java.util.UUID.fromString(id))
                .thenReturn("Franquicia eliminada correctamente");
    }

    /**
     * Elimina una franquicia por nombre
     * @param nombre nombre de la franquicia
     * @return Mono con un mensaje de confirmación
     */
    @Override
    public Mono<String> eliminarPorNombre(String nombre) {
        return repository.deleteByNombre(nombre)
                .thenReturn("Franquicia eliminada correctamente");
    }

    /**
     * Actualiza una franquicia
     * @param franquiciaId id de la franquicia a actualizar
     * @param cambios cambios a aplicar
     * @return Mono con la franquicia actualizada
     */
    @Override
    public Mono<Franquicia> actualizarFranquicia(String franquiciaId, Franquicia cambios) {
        return repository.findById(java.util.UUID.fromString(franquiciaId))
                .flatMap(existente -> {
                    existente.setNombre(cambios.getNombre());
                    existente.setUpdatedAt(Instant.now());
                    return repository.save(existente);
                })
                .map(this::toEntity)
                .onErrorMap(DuplicateKeyException.class,
                        e -> new IllegalStateException("El nombre de la franquicia ya existe", e));
    }

    /**
     * Cuenta el total de franquicias
     * @return Mono con el total
     */
    @Override
    public Mono<Long> contar() {
        return repository.countAll();
    }
}
