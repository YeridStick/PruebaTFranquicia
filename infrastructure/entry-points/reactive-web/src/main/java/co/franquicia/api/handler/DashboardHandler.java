package co.franquicia.api.handler;

import co.franquicia.usecase.dashboard.DashboardUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;

@Component
@RequiredArgsConstructor
public class DashboardHandler {

    private static final Logger logger = Logger.getLogger(DashboardHandler.class.getName());
    private final DashboardUseCase dashboardUseCase;

    /**
     * GET /api/metrics/resumen - métricas para gráficas
     */
    public Mono<ServerResponse> obtenerResumen(ServerRequest request) {
        return dashboardUseCase.obtenerResumen()
                .flatMap(resumen -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(resumen))
                .doOnError(e -> logger.severe("Error al obtener resumen de métricas: " + e.getMessage()));
    }
}
