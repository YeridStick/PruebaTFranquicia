package co.franquicia.model.sucursal.gateways;

import co.franquicia.model.sucursal.Sucursal;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SucursalRepository {
    Mono<Sucursal> crearSucursal(String franquiciaId, String nombre);
    Mono<Sucursal> obtenerPorId(String id);
    Flux<Sucursal> obtenerTodas();
    Flux<Sucursal> obtenerPorFranquicia(String franquiciaId);
    Flux<Sucursal> obtenerPorNombre(String nombre);
    Flux<Sucursal> buscarPorNombreContaining(String nombre);
    Mono<String> eliminarPorId(String id);
    Mono<Sucursal> actualizarSucursal(String sucursalId, Sucursal cambios);
    Mono<Long> contarPorFranquicia(String franquiciaId);
}
