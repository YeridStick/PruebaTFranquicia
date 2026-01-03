package co.franquicia.r2dbc.repository;

import co.franquicia.r2dbc.entity.FranquiciaData;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveFranquiciaRepository extends ReactiveCrudRepository<FranquiciaData, String>,
        ReactiveQueryByExampleExecutor<FranquiciaData> {

    /**
     * Busca una franquicia por nombre
     * @param nombre nombre de la franquicia
     * @return Mono con la franquicia encontrada
     */
    @Query("SELECT * FROM franquicia WHERE nombre = :nombre")
    Mono<FranquiciaData> findByNombre(String nombre);

    /**
     * Busca todas las franquicias que contengan el nombre especificado
     * @param nombre parte del nombre a buscar
     * @return Flux con todas las franquicias encontradas
     */
    @Query("SELECT * FROM franquicia WHERE nombre ILIKE '%' || :nombre || '%'")
    Flux<FranquiciaData> findByNombreContaining(String nombre);

    /**
     * Cuenta el total de franquicias
     * @return Mono con el total
     */
    @Query("SELECT COUNT(*) FROM franquicia")
    Mono<Long> countAll();

    /**
     * Elimina una franquicia por nombre
     * @param nombre nombre de la franquicia
     * @return Mono void
     */
    @Query("DELETE FROM franquicia WHERE nombre = :nombre")
    Mono<Void> deleteByNombre(String nombre);
}
