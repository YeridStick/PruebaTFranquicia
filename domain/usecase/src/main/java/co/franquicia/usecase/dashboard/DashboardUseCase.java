package co.franquicia.usecase.dashboard;

import co.franquicia.model.dashboard.DashboardMetrics;
import co.franquicia.model.franquicia.gateways.FranquiciaRepository;
import co.franquicia.model.producto.gateways.ProductoRepository;
import co.franquicia.model.sucursal.gateways.SucursalRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;

@RequiredArgsConstructor
public class DashboardUseCase {

    private static final Logger logger = Logger.getLogger(DashboardUseCase.class.getName());

    private final FranquiciaRepository franquiciaRepository;
    private final SucursalRepository sucursalRepository;
    private final ProductoRepository productoRepository;

    /**
     * Obtiene métricas básicas para gráficas
     * @return Mono con el resumen de métricas
     */
    public Mono<DashboardMetrics> obtenerResumen() {
        return Mono.zip(
                        franquiciaRepository.contar().defaultIfEmpty(0L),
                        sucursalRepository.contarTodas().defaultIfEmpty(0L),
                        productoRepository.contarTodos().defaultIfEmpty(0L),
                        productoRepository.sumarStock().defaultIfEmpty(0L)
                )
                .map(t -> DashboardMetrics.of(
                        t.getT1(),
                        t.getT2(),
                        t.getT3(),
                        t.getT4(),
                        0L // ventasTotales: no hay fuente de ventas en el modelo actual
                ))
                .doOnSubscribe(s -> logger.info("[dashboardResumen]"))
                .doOnError(e -> logger.severe("[dashboardResumen] error: " + e.getMessage()));
    }
}
