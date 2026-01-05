package co.franquicia.usecase.sucursal;

import co.franquicia.model.sucursal.Sucursal;
import co.franquicia.model.sucursal.gateways.SucursalRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;

@RequiredArgsConstructor
public class SucursalUseCase {

    private static final Logger logger = Logger.getLogger(SucursalUseCase.class.getName());
    private final SucursalRepository repository;

    /**
     * Crea una nueva sucursal para una franquicia
     * @param franquiciaId id de la franquicia
     * @param nombre nombre de la sucursal
     * @return Mono con la sucursal creada
     */
    public Mono<Sucursal> crearSucursal(String franquiciaId, String nombre) {
        return Mono.when(
                        validarId(franquiciaId, "ID de franquicia obligatorio"),
                        validarNombre(nombre, "El nombre de la sucursal es obligatorio")
                )
                .then(Mono.defer(() -> repository.crearSucursal(franquiciaId, nombre.trim())))
                .doOnSubscribe(s -> logger.info(() -> "[crearSucursal] franquiciaId=" + franquiciaId + ", nombre=" + nombre))
                .doOnSuccess(su -> logger.info(() -> "[crearSucursal] creada id=" + (su != null ? su.getId() : "null")))
                .doOnError(e -> logger.severe("[crearSucursal] error: " + e.getMessage()));
    }

    /**
     * Obtiene una sucursal por su ID
     * @param id id de la sucursal
     * @return Mono con la sucursal encontrada
     */
    public Mono<Sucursal> obtenerPorId(String id) {
        return validarId(id, "ID obligatorio")
                .then(Mono.defer(() -> repository.obtenerPorId(id)))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Sucursal no encontrada")))
                .doOnSubscribe(s -> logger.info(() -> "[obtenerPorId] id=" + id))
                .doOnError(e -> logger.severe("[obtenerPorId] error: " + e.getMessage()));
    }

    /**
     * Obtiene todas las sucursales
     * @return Flux con todas las sucursales
     */
    public Flux<Sucursal> obtenerTodas() {
        return repository.obtenerTodas()
                .doOnSubscribe(s -> logger.info("[obtenerTodas]"))
                .doOnError(e -> logger.severe("[obtenerTodas] error: " + e.getMessage()));
    }

    /**
     * Obtiene todas las sucursales de una franquicia
     * @param franquiciaId id de la franquicia
     * @return Flux con las sucursales de la franquicia
     */
    public Flux<Sucursal> obtenerPorFranquicia(String franquiciaId) {
        return validarId(franquiciaId, "ID de franquicia obligatorio")
                .thenMany(repository.obtenerPorFranquicia(franquiciaId))
                .doOnSubscribe(s -> logger.info(() -> "[obtenerPorFranquicia] franquiciaId=" + franquiciaId))
                .doOnError(e -> logger.severe("[obtenerPorFranquicia] error: " + e.getMessage()));
    }

    /**
     * Obtiene una sucursal por nombre
     * @param nombre nombre de la sucursal
     * @return Mono con la sucursal encontrada
     */
    public Flux<Sucursal> obtenerPorNombre(String nombre) {
        return validarNombre(nombre, "El nombre es obligatorio")
                .thenMany(repository.obtenerPorNombre(nombre.trim()))
                .doOnSubscribe(s -> logger.info(() -> "[obtenerPorNombre] nombre=" + nombre))
                .doOnError(e -> logger.severe("[obtenerPorNombre] error: " + e.getMessage()));
    }

    /**
     * Busca sucursales que contengan el nombre especificado
     * @param nombre parte del nombre a buscar
     * @return Flux con las sucursales encontradas
     */
    public Flux<Sucursal> buscarPorNombreContaining(String nombre) {
        return validarNombre(nombre, "El nombre es obligatorio")
                .thenMany(repository.buscarPorNombreContaining(nombre.trim()))
                .doOnSubscribe(s -> logger.info(() -> "[buscarPorNombreContaining] nombre=" + nombre))
                .doOnError(e -> logger.severe("[buscarPorNombreContaining] error: " + e.getMessage()));
    }

    /**
     * Actualiza una sucursal
     * @param sucursalId id de la sucursal a actualizar
     * @param cambios cambios a aplicar
     * @return Mono con la sucursal actualizada
     */
    public Mono<Sucursal> actualizarSucursal(String sucursalId, Sucursal cambios) {
        return validarId(sucursalId, "ID obligatorio")
                .then(Mono.defer(() -> {
                    if (cambios.getNombre() != null) {
                        String nombre = cambios.getNombre().trim();
                        if (nombre.isBlank()) {
                            return Mono.error(new IllegalArgumentException("El nombre de la sucursal no puede estar vacío"));
                        }
                        cambios.setNombre(nombre);
                    }
                    return repository.actualizarSucursal(sucursalId, cambios);
                }))
                .doOnSubscribe(s -> logger.info(() -> "[actualizarSucursal] id=" + sucursalId))
                .doOnError(e -> logger.severe("[actualizarSucursal] error: " + e.getMessage()));
    }

    /**
     * Elimina una sucursal por su ID
     * @param id id de la sucursal
     * @return Mono con un mensaje de confirmación
     */
    public Mono<String> eliminarPorId(String id) {
        return validarId(id, "ID obligatorio")
                .then(Mono.defer(() -> repository.eliminarPorId(id)))
                .doOnSubscribe(s -> logger.info(() -> "[eliminarPorId] id=" + id))
                .doOnError(e -> logger.severe("[eliminarPorId] error: " + e.getMessage()));
    }

    /**
     * Cuenta sucursales por franquicia
     * @param franquiciaId id de la franquicia
     * @return Mono con el total
     */
    public Mono<Long> contarPorFranquicia(String franquiciaId) {
        return validarId(franquiciaId, "ID de franquicia obligatorio")
                .then(Mono.defer(() -> repository.contarPorFranquicia(franquiciaId)))
                .doOnSubscribe(s -> logger.info(() -> "[contarPorFranquicia] franquiciaId=" + franquiciaId))
                .doOnError(e -> logger.severe("[contarPorFranquicia] error: " + e.getMessage()));
    }

    /**
     * Cuenta todas las sucursales
     * @return Mono con el total
     */
    public Mono<Long> contarTodas() {
        return repository.contarTodas()
                .doOnSubscribe(s -> logger.info("[contarTodasSucursales]"))
                .doOnError(e -> logger.severe("[contarTodasSucursales] error: " + e.getMessage()));
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
}
