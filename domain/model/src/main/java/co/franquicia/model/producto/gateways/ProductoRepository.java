package co.franquicia.model.producto.gateways;

import co.franquicia.model.producto.Producto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductoRepository {
    Mono<Producto> crearProducto(String sucursalId, String nombre, long precio, int stock);
    Mono<Producto> obtenerPorId(String id);
    Flux<Producto> obtenerTodos();
    Flux<Producto> obtenerPorSucursal(String sucursalId);
    Flux<Producto> obtenerPorNombre(String nombre);
    Flux<Producto> buscarPorNombreEnSucursal(String sucursalId, String nombre);
    Flux<Producto> buscarPorStockBajo(int stock);
    Mono<Producto> obtenerMasCaro(String sucursalId);
    Mono<String> eliminarPorId(String id);
    Mono<Producto> actualizarProducto(String productoId, Producto cambios);
    Mono<Long> contarPorSucursal(String sucursalId);
    Mono<Long> contarTodos();
    Mono<Long> sumarStock();
}
