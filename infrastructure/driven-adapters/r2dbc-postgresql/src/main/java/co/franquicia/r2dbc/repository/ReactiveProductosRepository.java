package co.franquicia.r2dbc.repository;

import co.franquicia.r2dbc.entity.ProductoData;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ReactiveProductosRepository extends ReactiveCrudRepository<ProductoData, UUID>,
        ReactiveQueryByExampleExecutor<ProductoData> {

    /**
     * Busca todos los productos de una sucursal
     * @param sucursalId id de la sucursal
     * @return Flux con todos los productos
     */
    @Query("SELECT * FROM producto WHERE sucursal_id = :sucursalId")
    Flux<ProductoData> findBySucursalId(java.util.UUID sucursalId);

    /**
     * Busca un producto por nombre
     * @param nombre nombre del producto
     * @return Mono con el producto encontrado
     */
    @Query("SELECT * FROM producto WHERE nombre ILIKE '%' || :nombre || '%'")
    Flux<ProductoData> findByNombre(String nombre);

    /**
     * Busca productos de una sucursal que contengan el nombre especificado
     * @param sucursalId id de la sucursal
     * @param nombre parte del nombre a buscar
     * @return Flux con los productos encontrados
     */
    @Query("SELECT * FROM producto WHERE sucursal_id = :sucursalId AND nombre ILIKE '%' || :nombre || '%'")
    Flux<ProductoData> findBySucursalIdAndNombreContaining(java.util.UUID sucursalId, String nombre);

    /**
     * Busca productos con stock menor al especificado
     * @param stock stock mínimo
     * @return Flux con los productos
     */
    @Query("SELECT * FROM producto WHERE stock < :stock")
    Flux<ProductoData> findByStockLessThan(int stock);

    /**
     * Cuenta productos por sucursal
     * @param sucursalId id de la sucursal
     * @return Mono con el total
     */
    @Query("SELECT COUNT(*) FROM producto WHERE sucursal_id = :sucursalId")
    Mono<Long> countBySucursalId(java.util.UUID sucursalId);

    /**
     * Busca el producto más caro de una sucursal
     * @param sucursalId id de la sucursal
     * @return Mono con el producto más caro
     */
    @Query("SELECT * FROM producto WHERE sucursal_id = :sucursalId ORDER BY precio DESC LIMIT 1")
    Mono<ProductoData> findMostExpensiveInSucursal(java.util.UUID sucursalId);
}
