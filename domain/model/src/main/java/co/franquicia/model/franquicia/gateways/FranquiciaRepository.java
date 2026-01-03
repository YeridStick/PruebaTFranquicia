package co.franquicia.model.franquicia.gateways;

import co.franquicia.model.franquicia.Franquicia;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FranquiciaRepository {
    Mono<Franquicia> crearFranquicia(String nombre);
    Mono<Franquicia> obtenerPorId(String id);
    Flux<Franquicia> obtenerFranquicias();
    Mono<Franquicia> obtenerPorNombre(String nombre);
    Flux<Franquicia> buscarPorNombreContaining(String nombre);
    Mono<String> eliminarPorId(String id);
    Mono<String> eliminarPorNombre(String nombre);
    Mono<Franquicia> actualizarFranquicia(String franquiciaId, Franquicia cambios);
    Mono<Long> contar();
}
