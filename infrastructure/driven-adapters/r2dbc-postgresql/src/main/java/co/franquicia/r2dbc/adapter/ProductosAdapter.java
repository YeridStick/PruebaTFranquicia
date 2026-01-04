package co.franquicia.r2dbc.adapter;

import co.franquicia.model.producto.Producto;
import co.franquicia.model.producto.gateways.ProductoRepository;
import co.franquicia.r2dbc.entity.ProductoData;
import co.franquicia.r2dbc.helper.ReactiveAdapterOperations;
import co.franquicia.r2dbc.repository.ReactiveProductosRepository;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Repository
public class ProductosAdapter extends ReactiveAdapterOperations<
        Producto,
        ProductoData,
        java.util.UUID,
        ReactiveProductosRepository
> implements ProductoRepository {

    public ProductosAdapter(ReactiveProductosRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> Producto.builder()
                .id(d.getId() != null ? d.getId().toString() : null)
                .sucursalId(d.getSucursalId() != null ? d.getSucursalId().toString() : null)
                .nombre(d.getNombre())
                .precio(d.getPrecio())
                .stock(d.getStock())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build());
    }

    /**
     * Crea un nuevo producto en una sucursal
     * @param sucursalId id de la sucursal
     * @param nombre nombre del producto
     * @param precio precio del producto
     * @param stock stock inicial
     * @return Mono con el producto creado
     */
    @Override
    public Mono<Producto> crearProducto(String sucursalId, String nombre, long precio, int stock) {
        java.util.UUID sucursalUuid = java.util.UUID.fromString(sucursalId);
        return repository.findByNombre(nombre).next()
                .flatMap(existing -> Mono.<ProductoData>error(new IllegalStateException("Producto ya existe")))
                .switchIfEmpty(Mono.defer(() -> {
                    var data = ProductoData.builder()
                            .sucursalId(sucursalUuid)
                            .nombre(nombre)
                            .precio(precio)
                            .stock(stock)
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build();
                    return repository.save(data);
                }))
                .map(this::toEntity)
                .onErrorMap(DuplicateKeyException.class,
                        e -> new IllegalStateException("El nombre del producto ya existe", e))
                .onErrorMap(DataIntegrityViolationException.class,
                        e -> new IllegalArgumentException("La sucursal no existe o es inválida", e));
    }

    /**
     * Obtiene un producto por su ID
     * @param id id del producto
     * @return Mono con el producto encontrado
     */
    @Override
    public Mono<Producto> obtenerPorId(String id) {
        return repository.findById(java.util.UUID.fromString(id))
                .map(this::toEntity);
    }

    /**
     * Obtiene todos los productos
     * @return Flux con todos los productos
     */
    @Override
    public Flux<Producto> obtenerTodos() {
        return findAll();
    }

    /**
     * Obtiene todos los productos de una sucursal
     * @param sucursalId id de la sucursal
     * @return Flux con los productos de la sucursal
     */
    @Override
    public Flux<Producto> obtenerPorSucursal(String sucursalId) {
        return repository.findBySucursalId(java.util.UUID.fromString(sucursalId))
                .map(this::toEntity);
    }

    /**
     * Obtiene un producto por nombre
     * @param nombre nombre del producto
     * @return Mono con el producto encontrado
     */
    @Override
    public Flux<Producto> obtenerPorNombre(String nombre) {
        return repository.findByNombre(nombre)
                .map(this::toEntity);
    }

    /**
     * Busca productos de una sucursal que contengan el nombre especificado
     * @param sucursalId id de la sucursal
     * @param nombre parte del nombre a buscar
     * @return Flux con los productos encontrados
     */
    @Override
    public Flux<Producto> buscarPorNombreEnSucursal(String sucursalId, String nombre) {
        return repository.findBySucursalIdAndNombreContaining(java.util.UUID.fromString(sucursalId), nombre)
                .map(this::toEntity);
    }

    /**
     * Busca productos con stock bajo
     * @param stock stock mínimo
     * @return Flux con los productos con stock bajo
     */
    @Override
    public Flux<Producto> buscarPorStockBajo(int stock) {
        return repository.findByStockLessThan(stock)
                .map(this::toEntity);
    }

    /**
     * Obtiene el producto más caro de una sucursal
     * @param sucursalId id de la sucursal
     * @return Mono con el producto más caro
     */
    @Override
    public Mono<Producto> obtenerMasCaro(String sucursalId) {
        return repository.findMostExpensiveInSucursal(java.util.UUID.fromString(sucursalId))
                .map(this::toEntity);
    }

    /**
     * Elimina un producto por su ID
     * @param id id del producto
     * @return Mono con un mensaje de confirmación
     */
    @Override
    public Mono<String> eliminarPorId(String id) {
        return repository.deleteById(java.util.UUID.fromString(id))
                .thenReturn("Producto eliminado correctamente");
    }

    /**
     * Actualiza un producto
     * @param productoId id del producto a actualizar
     * @param cambios cambios a aplicar
     * @return Mono con el producto actualizado
     */
    @Override
    public Mono<Producto> actualizarProducto(String productoId, Producto cambios) {
        return repository.findById(java.util.UUID.fromString(productoId))
                .flatMap(existente -> {
                    existente.setNombre(cambios.getNombre());
                    existente.setPrecio(cambios.getPrecio());
                    existente.setStock(cambios.getStock());
                    existente.setUpdatedAt(Instant.now());
                    return repository.save(existente);
                })
                .map(this::toEntity)
                .onErrorMap(DuplicateKeyException.class,
                        e -> new IllegalStateException("El nombre del producto ya existe", e));
    }

    /**
     * Cuenta productos por sucursal
     * @param sucursalId id de la sucursal
     * @return Mono con el total
     */
    @Override
    public Mono<Long> contarPorSucursal(String sucursalId) {
        return repository.countBySucursalId(java.util.UUID.fromString(sucursalId));
    }
}
