package co.franquicia.api.handler;

import co.franquicia.model.producto.Producto;
import co.franquicia.usecase.producto.ProductoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
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
                .onErrorResume(e -> {
                    logger.severe("Error al obtener todos los productos: " + e.getMessage());
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .bodyValue("Error al obtener productos");
                });
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
                .onErrorResume(IllegalArgumentException.class, e ->
                        ServerResponse.notFound().build())
                .onErrorResume(e -> {
                    logger.severe("Error al obtener producto por ID: " + e.getMessage());
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .bodyValue("Error al obtener producto");
                });
    }

    /**
     * GET /api/productos/nombre/{nombre} - Obtiene un producto por nombre
     */
    public Mono<ServerResponse> obtenerPorNombre(ServerRequest serverRequest) {
        String nombre = serverRequest.pathVariable("nombre");
        return productoUseCase.obtenerPorNombre(nombre)
                .flatMap(producto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(producto))
                .onErrorResume(IllegalArgumentException.class, e ->
                        ServerResponse.notFound().build())
                .onErrorResume(e -> {
                    logger.severe("Error al obtener producto por nombre: " + e.getMessage());
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .bodyValue("Error al obtener producto");
                });
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
                .onErrorResume(IllegalArgumentException.class, e ->
                        ServerResponse.badRequest().bodyValue("Error: " + e.getMessage()))
                .onErrorResume(e -> {
                    logger.severe("Error al obtener productos por sucursal: " + e.getMessage());
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .bodyValue("Error al obtener productos");
                });
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
                .onErrorResume(IllegalArgumentException.class, e ->
                        ServerResponse.badRequest().bodyValue("Error: " + e.getMessage()))
                .onErrorResume(e -> {
                    logger.severe("Error al buscar productos: " + e.getMessage());
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .bodyValue("Error al buscar productos");
                });
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
                    .onErrorResume(e -> {
                        logger.severe("Error al buscar productos con stock bajo: " + e.getMessage());
                        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .bodyValue("Error al buscar productos");
                    });
        } catch (NumberFormatException e) {
            return ServerResponse.badRequest().bodyValue("Stock debe ser un número válido");
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
                .onErrorResume(IllegalArgumentException.class, e ->
                        ServerResponse.badRequest().bodyValue("Error: " + e.getMessage()))
                .onErrorResume(e -> {
                    logger.severe("Error al obtener producto más caro: " + e.getMessage());
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .bodyValue("Error al obtener producto");
                });
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
                .onErrorResume(IllegalArgumentException.class, e ->
                        ServerResponse.badRequest().bodyValue("Error: " + e.getMessage()))
                .onErrorResume(e -> {
                    logger.severe("Error al crear producto: " + e.getMessage());
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .bodyValue("Error al crear producto");
                });
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
                .onErrorResume(IllegalArgumentException.class, e ->
                        ServerResponse.badRequest().bodyValue("Error: " + e.getMessage()))
                .onErrorResume(e -> {
                    logger.severe("Error al actualizar producto: " + e.getMessage());
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .bodyValue("Error al actualizar producto");
                });
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
                .onErrorResume(IllegalArgumentException.class, e ->
                        ServerResponse.badRequest().bodyValue("Error: " + e.getMessage()))
                .onErrorResume(e -> {
                    logger.severe("Error al eliminar producto: " + e.getMessage());
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .bodyValue("Error al eliminar producto");
                });
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
                .onErrorResume(IllegalArgumentException.class, e ->
                        ServerResponse.badRequest().bodyValue("Error: " + e.getMessage()))
                .onErrorResume(e -> {
                    logger.severe("Error al contar productos: " + e.getMessage());
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .bodyValue("Error al contar productos");
                });
    }
}