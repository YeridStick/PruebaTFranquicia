package co.franquicia.usecase.franquicia;

import co.franquicia.model.franquicia.Franquicia;
import co.franquicia.model.franquicia.gateways.FranquiciaRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;

@RequiredArgsConstructor
public class FranquiciaUseCase {

    private static final Logger logger = Logger.getLogger(FranquiciaUseCase.class.getName());
    private final FranquiciaRepository repository;

    /**
     * Crea una nueva franquicia
     * @param nombre nombre de la franquicia
     * @return Mono con la franquicia creada
     */
    public Mono<Franquicia> crearFranquicia(String nombre) {
        return validarNombre(nombre, "El nombre de la franquicia es obligatorio")
                .then(Mono.defer(() -> repository.crearFranquicia(nombre.trim())))
                .doOnSubscribe(s -> logger.info(() -> "[crearFranquicia] nombre=" + nombre))
                .doOnSuccess(f -> logger.info(() -> "[crearFranquicia] creada id=" + (f != null ? f.getId() : "null")))
                .doOnError(e -> logger.severe("[crearFranquicia] error: " + e.getMessage()));
    }

    /**
     * Obtiene una franquicia por su ID
     * @param id id de la franquicia
     * @return Mono con la franquicia encontrada
     */
    public Mono<Franquicia> obtenerPorId(String id) {
        return validarId(id)
                .then(Mono.defer(() -> repository.obtenerPorId(id)))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Franquicia no encontrada")))
                .doOnSubscribe(s -> logger.info(() -> "[obtenerPorId] id=" + id))
                .doOnError(e -> logger.severe("[obtenerPorId] error: " + e.getMessage()));
    }

    /**
     * Obtiene todas las franquicias
     * @return Flux con todas las franquicias
     */
    public Flux<Franquicia> obtenerTodas() {
        return repository.obtenerFranquicias()
                .doOnSubscribe(s -> logger.info("[obtenerTodas]"))
                .doOnError(e -> logger.severe("[obtenerTodas] error: " + e.getMessage()));
    }

    /**
     * Obtiene una franquicia por nombre
     * @param nombre nombre de la franquicia
     * @return Mono con la franquicia encontrada
     */
    public Mono<Franquicia> obtenerPorNombre(String nombre) {
        return validarNombre(nombre, "El nombre es obligatorio")
                .then(Mono.defer(() -> repository.obtenerPorNombre(nombre.trim())))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Franquicia no encontrada")))
                .doOnSubscribe(s -> logger.info(() -> "[obtenerPorNombre] nombre=" + nombre))
                .doOnError(e -> logger.severe("[obtenerPorNombre] error: " + e.getMessage()));
    }

    /**
     * Busca franquicias que contengan el nombre especificado
     * @param nombre parte del nombre a buscar
     * @return Flux con las franquicias encontradas
     */
    public Flux<Franquicia> buscarPorNombreContaining(String nombre) {
        return validarNombre(nombre, "El nombre es obligatorio")
                .thenMany(repository.buscarPorNombreContaining(nombre.trim()))
                .doOnSubscribe(s -> logger.info(() -> "[buscarPorNombreContaining] nombre=" + nombre))
                .doOnError(e -> logger.severe("[buscarPorNombreContaining] error: " + e.getMessage()));
    }

    /**
     * Actualiza una franquicia
     * @param franquiciaId id de la franquicia a actualizar
     * @param cambios cambios a aplicar
     * @return Mono con la franquicia actualizada
     */
    public Mono<Franquicia> actualizarFranquicia(String franquiciaId, Franquicia cambios) {
        return validarId(franquiciaId)
                .then(Mono.defer(() -> {
                    if (cambios.getNombre() != null) {
                        String nombre = cambios.getNombre().trim();
                        if (nombre.isBlank()) {
                            return Mono.error(new IllegalArgumentException("El nombre de la franquicia no puede estar vacío"));
                        }
                        cambios.setNombre(nombre);
                    }
                    return repository.actualizarFranquicia(franquiciaId, cambios);
                }))
                .doOnSubscribe(s -> logger.info(() -> "[actualizarFranquicia] id=" + franquiciaId))
                .doOnError(e -> logger.severe("[actualizarFranquicia] error: " + e.getMessage()));
    }

    /**
     * Elimina una franquicia por su ID
     * @param id id de la franquicia
     * @return Mono con un mensaje de confirmación
     */
    public Mono<String> eliminarPorId(String id) {
        return validarId(id)
                .then(Mono.defer(() -> repository.eliminarPorId(id)))
                .doOnSubscribe(s -> logger.info(() -> "[eliminarPorId] id=" + id))
                .doOnError(e -> logger.severe("[eliminarPorId] error: " + e.getMessage()));
    }

    /**
     * Elimina una franquicia por nombre
     * @param nombre nombre de la franquicia
     * @return Mono con un mensaje de confirmación
     */
    public Mono<String> eliminarPorNombre(String nombre) {
        return validarNombre(nombre, "El nombre es obligatorio")
                .then(Mono.defer(() -> repository.eliminarPorNombre(nombre.trim())))
                .doOnSubscribe(s -> logger.info(() -> "[eliminarPorNombre] nombre=" + nombre))
                .doOnError(e -> logger.severe("[eliminarPorNombre] error: " + e.getMessage()));
    }

    /**
     * Cuenta el total de franquicias
     * @return Mono con el total
     */
    public Mono<Long> contar() {
        return repository.contar()
                .doOnSubscribe(s -> logger.info("[contar]"))
                .doOnError(e -> logger.severe("[contar] error: " + e.getMessage()));
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
     * @return Mono void
     */
    private Mono<Void> validarId(String id) {
        return Mono.justOrEmpty(id)
                .filter(s -> !s.isBlank())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("ID es obligatorio")))
                .then();
    }
}
