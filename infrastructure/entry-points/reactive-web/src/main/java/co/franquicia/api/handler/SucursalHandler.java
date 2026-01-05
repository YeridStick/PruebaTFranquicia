package co.franquicia.api.handler;

import co.franquicia.model.sucursal.Sucursal;
import co.franquicia.usecase.sucursal.SucursalUseCase;
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
public class SucursalHandler {

    private static final Logger logger = Logger.getLogger(SucursalHandler.class.getName());
    private final SucursalUseCase sucursalUseCase;

    /**
     * GET /api/sucursales - Obtiene todas las sucursales
     */
    public Mono<ServerResponse> obtenerTodas(ServerRequest serverRequest) {
        return sucursalUseCase.obtenerTodas()
                .collectList()
                .flatMap(sucursales -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(sucursales))
                .doOnError(e -> logger.severe("Error al obtener todas las sucursales: " + e.getMessage()));
    }

    /**
     * GET /api/sucursales/{id} - Obtiene una sucursal por ID
     */
    public Mono<ServerResponse> obtenerPorId(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");
        return sucursalUseCase.obtenerPorId(id)
                .flatMap(sucursal -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(sucursal))
                .onErrorMap(IllegalArgumentException.class, e ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e))
                .doOnError(e -> logger.severe("Error al obtener sucursal por ID: " + e.getMessage()));
    }

    /**
     * GET /api/sucursales/nombre/{nombre} - Obtiene una sucursal por nombre
     */
    public Mono<ServerResponse> obtenerPorNombre(ServerRequest serverRequest) {
        String nombre = serverRequest.pathVariable("nombre");
        return sucursalUseCase.obtenerPorNombre(nombre)
                .collectList()
                .flatMap(sucursales -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(sucursales))
                .doOnError(e -> logger.severe("Error al obtener sucursal por nombre: " + e.getMessage()));
    }

    /**
     * GET /api/franquicias/{franquiciaId}/sucursales - Obtiene todas las sucursales de una franquicia
     */
    public Mono<ServerResponse> obtenerPorFranquicia(ServerRequest serverRequest) {
        String franquiciaId = serverRequest.pathVariable("franquiciaId");
        return sucursalUseCase.obtenerPorFranquicia(franquiciaId)
                .collectList()
                .flatMap(sucursales -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(sucursales))
                .onErrorMap(IllegalArgumentException.class, e ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e))
                .doOnError(e -> logger.severe("Error al obtener sucursales por franquicia: " + e.getMessage()));
    }

    /**
     * GET /api/sucursales/buscar/{nombre} - Busca sucursales por nombre (LIKE)
     */
    public Mono<ServerResponse> buscarPorNombreContaining(ServerRequest serverRequest) {
        String nombre = serverRequest.pathVariable("nombre");
        return sucursalUseCase.buscarPorNombreContaining(nombre)
                .collectList()
                .flatMap(sucursales -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(sucursales))
                .doOnError(e -> logger.severe("Error al buscar sucursales: " + e.getMessage()));
    }

    /**
     * POST /api/franquicias/{franquiciaId}/sucursales - Crea una nueva sucursal
     */
    public Mono<ServerResponse> crearSucursal(ServerRequest serverRequest) {
        String franquiciaId = serverRequest.pathVariable("franquiciaId");
        return serverRequest.bodyToMono(Sucursal.class)
                .flatMap(sucursal -> sucursalUseCase.crearSucursal(franquiciaId, sucursal.getNombre()))
                .flatMap(sucursalBd -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(sucursalBd))
                .onErrorMap(IllegalArgumentException.class, e ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e))
                .doOnError(e -> logger.severe("Error al crear sucursal: " + e.getMessage()));
    }

    /**
     * PUT /api/sucursales/{id} - Actualiza una sucursal
     */
    public Mono<ServerResponse> actualizarSucursal(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");
        return serverRequest.bodyToMono(Sucursal.class)
                .flatMap(sucursal -> sucursalUseCase.actualizarSucursal(id, sucursal))
                .flatMap(sucursalActualizada -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(sucursalActualizada))
                .onErrorMap(IllegalArgumentException.class, e ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e))
                .doOnError(e -> logger.severe("Error al actualizar sucursal: " + e.getMessage()));
    }

    /**
     * DELETE /api/sucursales/{id} - Elimina una sucursal por ID
     */
    public Mono<ServerResponse> eliminarPorId(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");
        return sucursalUseCase.eliminarPorId(id)
                .flatMap(mensaje -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(mensaje))
                .onErrorMap(IllegalArgumentException.class, e ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e))
                .doOnError(e -> logger.severe("Error al eliminar sucursal: " + e.getMessage()));
    }

    /**
     * GET /api/franquicias/{franquiciaId}/sucursales/contar - Cuenta sucursales por franquicia
     */
    public Mono<ServerResponse> contarPorFranquicia(ServerRequest serverRequest) {
        String franquiciaId = serverRequest.pathVariable("franquiciaId");
        return sucursalUseCase.contarPorFranquicia(franquiciaId)
                .flatMap(total -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(total))
                .onErrorMap(IllegalArgumentException.class, e ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e))
                .doOnError(e -> logger.severe("Error al contar sucursales: " + e.getMessage()));
    }
}
