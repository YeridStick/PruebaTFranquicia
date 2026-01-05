package co.franquicia.api.handler;

import co.franquicia.model.producto.Producto;
import co.franquicia.usecase.producto.ProductoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;

@Component
@RequiredArgsConstructor
public class ProductoHandler {

    private static final Logger logger = Logger.getLogger(ProductoHandler.class.getName());
    private final ProductoUseCase productoUseCase;

    /**
     * GET /api/productos - Obtiene todos los productos
     */
    public Mono<ServerResponse> obtenerTodos(ServerRequest serverRequest) {
        return productoUseCase.obtenerTodos()
                .collectList()
                .flatMap(productos -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(productos))
                .doOnError(e -> logger.severe("Error al obtener todos los productos: " + e.getMessage()));
    }

    /**
     * GET /api/productos/{id} - Obtiene un producto por ID
     */
    public Mono<ServerResponse> obtenerPorId(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");
        return productoUseCase.obtenerPorId(id)
                .flatMap(producto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(producto))
                .onErrorMap(IllegalArgumentException.class, e ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e))
                .doOnError(e -> logger.severe("Error al obtener producto por ID: " + e.getMessage()));
    }

    /**
     * GET /api/productos/nombre/{nombre} - Obtiene un producto por nombre
     */
    public Mono<ServerResponse> obtenerPorNombre(ServerRequest serverRequest) {
        String nombre = serverRequest.pathVariable("nombre");
        return productoUseCase.obtenerPorNombre(nombre)
                .collectList()
                .flatMap(productos -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(productos))
                .doOnError(e -> logger.severe("Error al obtener producto por nombre: " + e.getMessage()));
    }

    /**
     * GET /api/sucursales/{sucursalId}/productos - Obtiene todos los productos de una sucursal
     */
    public Mono<ServerResponse> obtenerPorSucursal(ServerRequest serverRequest) {
        String sucursalId = serverRequest.pathVariable("sucursalId");
        return productoUseCase.obtenerPorSucursal(sucursalId)
                .collectList()
                .flatMap(productos -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(productos))
                .onErrorMap(IllegalArgumentException.class, e ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e))
                .doOnError(e -> logger.severe("Error al obtener productos por sucursal: " + e.getMessage()));
    }

    /**
     * GET /api/sucursales/{sucursalId}/productos/buscar/{nombre} - Busca productos por nombre en una sucursal
     */
    public Mono<ServerResponse> buscarPorNombreEnSucursal(ServerRequest serverRequest) {
        String sucursalId = serverRequest.pathVariable("sucursalId");
        String nombre = serverRequest.pathVariable("nombre");
        return productoUseCase.buscarPorNombreEnSucursal(sucursalId, nombre)
                .collectList()
                .flatMap(productos -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(productos))
                .onErrorMap(IllegalArgumentException.class, e ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e))
                .doOnError(e -> logger.severe("Error al buscar productos: " + e.getMessage()));
    }

    /**
     * GET /api/productos/stock-bajo/{stock} - Busca productos con stock bajo
     */
    public Mono<ServerResponse> buscarPorStockBajo(ServerRequest serverRequest) {
        String stockStr = serverRequest.pathVariable("stock");
        try {
            int stock = Integer.parseInt(stockStr);
            return productoUseCase.buscarPorStockBajo(stock)
                    .collectList()
                    .flatMap(productos -> ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(productos))
                    .doOnError(e -> logger.severe("Error al buscar productos con stock bajo: " + e.getMessage()));
        } catch (NumberFormatException e) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock debe ser un número válido", e));
        }
    }

    /**
     * GET /api/sucursales/{sucursalId}/productos/mas-caro - Obtiene el producto más caro de una sucursal
     */
    public Mono<ServerResponse> obtenerMasCaro(ServerRequest serverRequest) {
        String sucursalId = serverRequest.pathVariable("sucursalId");
        return productoUseCase.obtenerMasCaro(sucursalId)
                .flatMap(producto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(producto))
                .onErrorMap(IllegalArgumentException.class, e ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e))
                .doOnError(e -> logger.severe("Error al obtener producto más caro: " + e.getMessage()));
    }

    /**
     * POST /api/sucursales/{sucursalId}/productos - Crea un nuevo producto
     */
    public Mono<ServerResponse> crearProducto(ServerRequest serverRequest) {
        String sucursalId = serverRequest.pathVariable("sucursalId");
        return serverRequest.bodyToMono(Producto.class)
                .flatMap(producto -> productoUseCase.crearProducto(
                        sucursalId,
                        producto.getNombre(),
                        producto.getPrecio(),
                        producto.getStock()
                ))
                .flatMap(productoBd -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(productoBd))
                .onErrorMap(IllegalArgumentException.class, e ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e))
                .doOnError(e -> logger.severe("Error al crear producto: " + e.getMessage()));
    }

    /**
     * PUT /api/productos/{id} - Actualiza un producto
     */
    public Mono<ServerResponse> actualizarProducto(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");
        return serverRequest.bodyToMono(Producto.class)
                .flatMap(producto -> productoUseCase.actualizarProducto(id, producto))
                .flatMap(productoActualizado -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(productoActualizado))
                .onErrorMap(IllegalArgumentException.class, e ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e))
                .doOnError(e -> logger.severe("Error al actualizar producto: " + e.getMessage()));
    }

    /**
     * DELETE /api/productos/{id} - Elimina un producto por ID
     */
    public Mono<ServerResponse> eliminarPorId(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");
        return productoUseCase.eliminarPorId(id)
                .flatMap(mensaje -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(mensaje))
                .onErrorMap(IllegalArgumentException.class, e ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e))
                .doOnError(e -> logger.severe("Error al eliminar producto: " + e.getMessage()));
    }

    /**
     * GET /api/sucursales/{sucursalId}/productos/contar - Cuenta productos por sucursal
     */
    public Mono<ServerResponse> contarPorSucursal(ServerRequest serverRequest) {
        String sucursalId = serverRequest.pathVariable("sucursalId");
        return productoUseCase.contarPorSucursal(sucursalId)
                .flatMap(total -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(total))
                .onErrorMap(IllegalArgumentException.class, e ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e))
                .doOnError(e -> logger.severe("Error al contar productos: " + e.getMessage()));
    }
}
