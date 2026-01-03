package co.franquicia.api.router;

import co.franquicia.api.handler.FranquiciaHandler;
import co.franquicia.api.handler.ProductoHandler;
import co.franquicia.api.handler.SucursalHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {

    /**
     * Configura todas las rutas de la aplicación
     */
    @Bean
    public RouterFunction<ServerResponse> routerFunction(
            FranquiciaHandler franquiciaHandler,
            SucursalHandler sucursalHandler,
            ProductoHandler productoHandler) {

        return route(GET("/api/franquicias"), franquiciaHandler::obtenerTodas)
                .andRoute(GET("/api/franquicias/{id}"), franquiciaHandler::obtenerPorId)
                .andRoute(GET("/api/franquicias/nombre/{nombre}"), franquiciaHandler::obtenerPorNombre)
                .andRoute(GET("/api/franquicias/buscar/{nombre}"), franquiciaHandler::buscarPorNombreContaining)
                .andRoute(GET("/api/franquicias/contar"), franquiciaHandler::contar)
                .andRoute(POST("/api/franquicias"), franquiciaHandler::crearFranquicia)
                .andRoute(PUT("/api/franquicias/{id}"), franquiciaHandler::actualizarFranquicia)
                .andRoute(DELETE("/api/franquicias/{id}"), franquiciaHandler::eliminarPorId)
                .andRoute(DELETE("/api/franquicias/nombre/{nombre}"), franquiciaHandler::eliminarPorNombre)

                // ============= SUCURSALES =============
                .andRoute(GET("/api/sucursales"), sucursalHandler::obtenerTodas)
                .andRoute(GET("/api/sucursales/{id}"), sucursalHandler::obtenerPorId)
                .andRoute(GET("/api/sucursales/nombre/{nombre}"), sucursalHandler::obtenerPorNombre)
                .andRoute(GET("/api/sucursales/buscar/{nombre}"), sucursalHandler::buscarPorNombreContaining)
                .andRoute(GET("/api/franquicias/{franquiciaId}/sucursales"), sucursalHandler::obtenerPorFranquicia)
                .andRoute(GET("/api/franquicias/{franquiciaId}/sucursales/contar"), sucursalHandler::contarPorFranquicia)
                .andRoute(POST("/api/franquicias/{franquiciaId}/sucursales"), sucursalHandler::crearSucursal)
                .andRoute(PUT("/api/sucursales/{id}"), sucursalHandler::actualizarSucursal)
                .andRoute(DELETE("/api/sucursales/{id}"), sucursalHandler::eliminarPorId)

                // ============= PRODUCTOS =============
                .andRoute(GET("/api/productos"), productoHandler::obtenerTodos)
                .andRoute(GET("/api/productos/{id}"), productoHandler::obtenerPorId)
                .andRoute(GET("/api/productos/nombre/{nombre}"), productoHandler::obtenerPorNombre)
                .andRoute(GET("/api/productos/stock-bajo/{stock}"), productoHandler::buscarPorStockBajo)
                .andRoute(GET("/api/sucursales/{sucursalId}/productos"), productoHandler::obtenerPorSucursal)
                .andRoute(GET("/api/sucursales/{sucursalId}/productos/buscar/{nombre}"), productoHandler::buscarPorNombreEnSucursal)
                .andRoute(GET("/api/sucursales/{sucursalId}/productos/mas-caro"), productoHandler::obtenerMasCaro)
                .andRoute(GET("/api/sucursales/{sucursalId}/productos/contar"), productoHandler::contarPorSucursal)
                .andRoute(POST("/api/sucursales/{sucursalId}/productos"), productoHandler::crearProducto)
                .andRoute(PUT("/api/productos/{id}"), productoHandler::actualizarProducto)
                .andRoute(DELETE("/api/productos/{id}"), productoHandler::eliminarPorId);
    }
}