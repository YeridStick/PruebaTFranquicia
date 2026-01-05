package co.franquicia.model.dashboard;

public record DashboardMetrics(
        long totalFranquicias,
        long totalSucursales,
        long totalProductos,
        long stockTotal,
        long ventasTotales
) {
    public static DashboardMetrics of(long totalFranquicias,
                                      long totalSucursales,
                                      long totalProductos,
                                      long stockTotal,
                                      long ventasTotales) {
        return new DashboardMetrics(totalFranquicias, totalSucursales, totalProductos, stockTotal, ventasTotales);
    }
}
