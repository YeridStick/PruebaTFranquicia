package co.franquicia.usecase.producto;

import co.franquicia.model.producto.Producto;
import co.franquicia.model.producto.gateways.ProductoRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;

@RequiredArgsConstructor
public class ProductoUseCase {

    private static final Logger logger = Logger.getLogger(ProductoUseCase.class.getName());
    private final ProductoRepository repository;

    /**
     * Crea un nuevo producto en una sucursal
     * @param sucursalId id de la sucursal
     * @param nombre nombre del producto
     * @param precio precio del producto
     * @param stock stock inicial
     * @return Mono con el producto creado
     */
    public Mono<Producto> crearProducto(String sucursalId, String nombre, long precio, int stock) {
        return Mono.when(
                        validarId(sucursalId, "ID de sucursal obligatorio"),
                        validarNombre(nombre, "El nombre del producto es obligatorio"),
                        validarPrecio(precio),
                        validarStock(stock)
                )
                .then(Mono.defer(() -> repository.crearProducto(sucursalId, nombre.trim(), precio, stock)))
                .doOnSubscribe(s -> logger.info(() ->
                        "[crearProducto] sucursalId=" + sucursalId + ", nombre=" + nombre + ", precio=" + precio + ", stock=" + stock))
                .doOnSuccess(p -> logger.info(() -> "[crearProducto] creado id=" + (p != null ? p.getId() : "null")))
                .doOnError(e -> logger.severe("[crearProducto] error: " + e.getMessage()));
    }

    /**
     * Obtiene un producto por su ID
     * @param id id del producto
     * @return Mono con el producto encontrado
     */
    public Mono<Producto> obtenerPorId(String id) {
        return validarId(id, "ID obligatorio")
                .then(Mono.defer(() -> repository.obtenerPorId(id)))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Producto no encontrado")))
                .doOnSubscribe(s -> logger.info(() -> "[obtenerPorId] id=" + id))
                .doOnError(e -> logger.severe("[obtenerPorId] error: " + e.getMessage()));
    }

    /**
     * Obtiene todos los productos
     * @return Flux con todos los productos
     */
    public Flux<Producto> obtenerTodos() {
        return repository.obtenerTodos()
                .doOnSubscribe(s -> logger.info("[obtenerTodos]"))
                .doOnError(e -> logger.severe("[obtenerTodos] error: " + e.getMessage()));
    }

    /**
     * Obtiene todos los productos de una sucursal
     * @param sucursalId id de la sucursal
     * @return Flux con los productos de la sucursal
     */
    public Flux<Producto> obtenerPorSucursal(String sucursalId) {
        return validarId(sucursalId, "ID de sucursal obligatorio")
                .thenMany(repository.obtenerPorSucursal(sucursalId))
                .doOnSubscribe(s -> logger.info(() -> "[obtenerPorSucursal] sucursalId=" + sucursalId))
                .doOnError(e -> logger.severe("[obtenerPorSucursal] error: " + e.getMessage()));
    }

    /**
     * Obtiene un producto por nombre
     * @param nombre nombre del producto
     * @return Mono con el producto encontrado
     */
    public Flux<Producto> obtenerPorNombre(String nombre) {
        return validarNombre(nombre, "El nombre es obligatorio")
                .thenMany(repository.obtenerPorNombre(nombre.trim()))
                .doOnSubscribe(s -> logger.info(() -> "[obtenerPorNombre] nombre=" + nombre))
                .doOnError(e -> logger.severe("[obtenerPorNombre] error: " + e.getMessage()));
    }

    /**
     * Busca productos de una sucursal que contengan el nombre especificado
     * @param sucursalId id de la sucursal
     * @param nombre parte del nombre a buscar
     * @return Flux con los productos encontrados
     */
    public Flux<Producto> buscarPorNombreEnSucursal(String sucursalId, String nombre) {
        return Mono.when(
                        validarId(sucursalId, "ID de sucursal obligatorio"),
                        validarNombre(nombre, "El nombre es obligatorio")
                )
                .thenMany(repository.buscarPorNombreEnSucursal(sucursalId, nombre.trim()))
                .doOnSubscribe(s -> logger.info(() ->
                        "[buscarPorNombreEnSucursal] sucursalId=" + sucursalId + ", nombre=" + nombre))
                .doOnError(e -> logger.severe("[buscarPorNombreEnSucursal] error: " + e.getMessage()));
    }

    /**
     * Busca productos con stock bajo
     * @param stock stock mínimo
     * @return Flux con los productos con stock bajo
     */
    public Flux<Producto> buscarPorStockBajo(int stock) {
        return validarStock(stock)
                .thenMany(repository.buscarPorStockBajo(stock))
                .doOnSubscribe(s -> logger.info(() -> "[buscarPorStockBajo] stock=" + stock))
                .doOnError(e -> logger.severe("[buscarPorStockBajo] error: " + e.getMessage()));
    }

    /**
     * Obtiene el producto más caro de una sucursal
     * @param sucursalId id de la sucursal
     * @return Mono con el producto más caro
     */
    public Mono<Producto> obtenerMasCaro(String sucursalId) {
        return validarId(sucursalId, "ID de sucursal obligatorio")
                .then(Mono.defer(() -> repository.obtenerMasCaro(sucursalId)))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("No hay productos en esta sucursal")))
                .doOnSubscribe(s -> logger.info(() -> "[obtenerMasCaro] sucursalId=" + sucursalId))
                .doOnError(e -> logger.severe("[obtenerMasCaro] error: " + e.getMessage()));
    }

    /**
     * Actualiza un producto
     * @param productoId id del producto a actualizar
     * @param cambios cambios a aplicar
     * @return Mono con el producto actualizado
     */
    public Mono<Producto> actualizarProducto(String productoId, Producto cambios) {
        return validarId(productoId, "ID obligatorio")
                .then(Mono.defer(() -> {
                    if (cambios.getNombre() != null) {
                        String nombre = cambios.getNombre().trim();
                        if (nombre.isBlank()) {
                            return Mono.error(new IllegalArgumentException("El nombre del producto no puede estar vacío"));
                        }
                        cambios.setNombre(nombre);
                    }

                    if (cambios.getPrecio() < 0) {
                        return Mono.error(new IllegalArgumentException("El precio no puede ser negativo"));
                    }

                    if (cambios.getStock() < 0) {
                        return Mono.error(new IllegalArgumentException("El stock no puede ser negativo"));
                    }

                    return repository.actualizarProducto(productoId, cambios);
                }))
                .doOnSubscribe(s -> logger.info(() -> "[actualizarProducto] id=" + productoId))
                .doOnError(e -> logger.severe("[actualizarProducto] error: " + e.getMessage()));
    }

    /**
     * Elimina un producto por su ID
     * @param id id del producto
     * @return Mono con un mensaje de confirmación
     */
    public Mono<String> eliminarPorId(String id) {
        return validarId(id, "ID obligatorio")
                .then(Mono.defer(() -> repository.eliminarPorId(id)))
                .doOnSubscribe(s -> logger.info(() -> "[eliminarPorId] id=" + id))
                .doOnError(e -> logger.severe("[eliminarPorId] error: " + e.getMessage()));
    }

    /**
     * Cuenta productos por sucursal
     * @param sucursalId id de la sucursal
     * @return Mono con el total
     */
    public Mono<Long> contarPorSucursal(String sucursalId) {
        return validarId(sucursalId, "ID de sucursal obligatorio")
                .then(Mono.defer(() -> repository.contarPorSucursal(sucursalId)))
                .doOnSubscribe(s -> logger.info(() -> "[contarPorSucursal] sucursalId=" + sucursalId))
                .doOnError(e -> logger.severe("[contarPorSucursal] error: " + e.getMessage()));
    }

    /**
     * Valida que el nombre no sea nulo ni vacío
     * @param valor valor a validar
     * @param mensaje mensaje de error
     * @return Mono void
     */
    private Mono<Void> validarNombre(String valor, String mensaje) {
        return Mono.justOrEmpty(valor)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .switchIfEmpty(Mono.error(new IllegalArgumentException(mensaje)))
                .then();
    }

    /**
     * Valida que el ID no sea nulo ni vacío
     * @param id id a validar
     * @param mensaje mensaje de error
     * @return Mono void
     */
    private Mono<Void> validarId(String id, String mensaje) {
        return Mono.justOrEmpty(id)
                .filter(s -> !s.isBlank())
                .switchIfEmpty(Mono.error(new IllegalArgumentException(mensaje)))
                .then();
    }

    /**
     * Valida que el precio sea válido
     * @param precio precio a validar
     * @return Mono void
     */
    private Mono<Void> validarPrecio(long precio) {
        return Mono.just(precio)
                .filter(p -> p > 0)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El precio debe ser mayor a 0")))
                .then();
    }

    /**
     * Valida que el stock no sea negativo
     * @param stock stock a validar
     * @return Mono void
     */
    private Mono<Void> validarStock(int stock) {
        return Mono.just(stock)
                .filter(s -> s >= 0)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El stock no puede ser negativo")))
                .then();
    }
}
