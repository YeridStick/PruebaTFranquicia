package co.franquicia.r2dbc.repository;

import co.franquicia.r2dbc.entity.SucursalData;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveSucursalesRepository extends ReactiveCrudRepository<SucursalData, String>,
        ReactiveQueryByExampleExecutor<SucursalData> {

    /**
     * Busca todas las sucursales de una franquicia
     * @param franquiciaId id de la franquicia
     * @return Flux con todas las sucursales
     */
    @Query("SELECT * FROM sucursal WHERE franquicia_id = :franquiciaId")
    Flux<SucursalData> findByFranquiciaId(String franquiciaId);

    /**
     * Busca una sucursal por nombre
     * @param nombre nombre de la sucursal
     * @return Mono con la sucursal encontrada
     */
    @Query("SELECT * FROM sucursal WHERE nombre = :nombre")
    Mono<SucursalData> findByNombre(String nombre);

    /**
     * Busca sucursales por nombre y franquicia
     * @param nombre nombre de la sucursal
     * @param franquiciaId id de la franquicia
     * @return Mono con la sucursal encontrada
     */
    @Query("SELECT * FROM sucursal WHERE nombre = :nombre AND franquicia_id = :franquiciaId")
    Mono<SucursalData> findByNombreAndFranquiciaId(String nombre, String franquiciaId);

    /**
     * Cuenta sucursales por franquicia
     * @param franquiciaId id de la franquicia
     * @return Mono con el total
     */
    @Query("SELECT COUNT(*) FROM sucursal WHERE franquicia_id = :franquiciaId")
    Mono<Long> countByFranquiciaId(String franquiciaId);

    /**
     * Busca sucursales que contengan el nombre especificado
     * @param nombre parte del nombre a buscar
     * @return Flux con todas las sucursales encontradas
     */
    @Query("SELECT * FROM sucursal WHERE nombre ILIKE '%' || :nombre || '%'")
    Flux<SucursalData> findByNombreContaining(String nombre);
}