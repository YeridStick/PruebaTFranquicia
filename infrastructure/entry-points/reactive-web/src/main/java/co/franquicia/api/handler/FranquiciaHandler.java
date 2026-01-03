package co.franquicia.api.handler;

import co.franquicia.model.franquicia.Franquicia;
import co.franquicia.usecase.franquicia.FranquiciaUseCase;
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
public class FranquiciaHandler {

    private static final Logger logger = Logger.getLogger(FranquiciaHandler.class.getName());
    private final FranquiciaUseCase franquiciaUseCase;

    /**
     * GET /api/franquicias - Obtiene todas las franquicias
     */
    public Mono<ServerResponse> obtenerTodas(ServerRequest serverRequest) {
        return franquiciaUseCase.obtenerTodas()
                .collectList()
                .flatMap(franquicias -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(franquicias))
                .onErrorResume(e -> {
                    logger.severe("Error al obtener todas las franquicias: " + e.getMessage());
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .bodyValue("Error al obtener franquicias");
                });
    }

    /**
     * GET /api/franquicias/{id} - Obtiene una franquicia por ID
     */
    public Mono<ServerResponse> obtenerPorId(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");
        return franquiciaUseCase.obtenerPorId(id)
                .flatMap(franquicia -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(franquicia))
                .onErrorResume(IllegalArgumentException.class, e ->
                        ServerResponse.notFound().build())
                .onErrorResume(e -> {
                    logger.severe("Error al obtener franquicia por ID: " + e.getMessage());
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .bodyValue("Error al obtener franquicia");
                });
    }

    /**
     * GET /api/franquicias/nombre/{nombre} - Obtiene una franquicia por nombre
     */
    public Mono<ServerResponse> obtenerPorNombre(ServerRequest serverRequest) {
        String nombre = serverRequest.pathVariable("nombre");
        return franquiciaUseCase.obtenerPorNombre(nombre)
                .flatMap(franquicia -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(franquicia))
                .onErrorResume(IllegalArgumentException.class, e ->
                        ServerResponse.notFound().build())
                .onErrorResume(e -> {
                    logger.severe("Error al obtener franquicia por nombre: " + e.getMessage());
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .bodyValue("Error al obtener franquicia");
                });
    }

    /**
     * GET /api/franquicias/buscar/{nombre} - Busca franquicias por nombre (LIKE)
     */
    public Mono<ServerResponse> buscarPorNombreContaining(ServerRequest serverRequest) {
        String nombre = serverRequest.pathVariable("nombre");
        return franquiciaUseCase.buscarPorNombreContaining(nombre)
                .collectList()
                .flatMap(franquicias -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(franquicias))
                .onErrorResume(e -> {
                    logger.severe("Error al buscar franquicias: " + e.getMessage());
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .bodyValue("Error al buscar franquicias");
                });
    }

    /**
     * POST /api/franquicias - Crea una nueva franquicia
     */
    public Mono<ServerResponse> crearFranquicia(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(Franquicia.class)
                .flatMap(franquicia -> franquiciaUseCase.crearFranquicia(franquicia.getNombre()))
                .flatMap(franquiciaBd -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(franquiciaBd))
                .onErrorResume(IllegalArgumentException.class, e ->
                        ServerResponse.badRequest().bodyValue("Error: " + e.getMessage()))
                .onErrorResume(e -> {
                    logger.severe("Error al crear franquicia: " + e.getMessage());
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .bodyValue("Error al crear franquicia");
                });
    }

    /**
     * PUT /api/franquicias/{id} - Actualiza una franquicia
     */
    public Mono<ServerResponse> actualizarFranquicia(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");
        return serverRequest.bodyToMono(Franquicia.class)
                .flatMap(franquicia -> franquiciaUseCase.actualizarFranquicia(id, franquicia))
                .flatMap(franquiciaActualizada -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(franquiciaActualizada))
                .onErrorResume(IllegalArgumentException.class, e ->
                        ServerResponse.badRequest().bodyValue("Error: " + e.getMessage()))
                .onErrorResume(e -> {
                    logger.severe("Error al actualizar franquicia: " + e.getMessage());
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .bodyValue("Error al actualizar franquicia");
                });
    }

    /**
     * DELETE /api/franquicias/{id} - Elimina una franquicia por ID
     */
    public Mono<ServerResponse> eliminarPorId(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");
        return franquiciaUseCase.eliminarPorId(id)
                .flatMap(mensaje -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(mensaje))
                .onErrorResume(IllegalArgumentException.class, e ->
                        ServerResponse.badRequest().bodyValue("Error: " + e.getMessage()))
                .onErrorResume(e -> {
                    logger.severe("Error al eliminar franquicia: " + e.getMessage());
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .bodyValue("Error al eliminar franquicia");
                });
    }

    /**
     * DELETE /api/franquicias/nombre/{nombre} - Elimina una franquicia por nombre
     */
    public Mono<ServerResponse> eliminarPorNombre(ServerRequest serverRequest) {
        String nombre = serverRequest.pathVariable("nombre");
        return franquiciaUseCase.eliminarPorNombre(nombre)
                .flatMap(mensaje -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(mensaje))
                .onErrorResume(IllegalArgumentException.class, e ->
                        ServerResponse.badRequest().bodyValue("Error: " + e.getMessage()))
                .onErrorResume(e -> {
                    logger.severe("Error al eliminar franquicia: " + e.getMessage());
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .bodyValue("Error al eliminar franquicia");
                });
    }

    /**
     * GET /api/franquicias/contar - Cuenta el total de franquicias
     */
    public Mono<ServerResponse> contar(ServerRequest serverRequest) {
        return franquiciaUseCase.contar()
                .flatMap(total -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(total))
                .onErrorResume(e -> {
                    logger.severe("Error al contar franquicias: " + e.getMessage());
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .bodyValue("Error al contar franquicias");
                });
    }
}