package co.franquicia.api.dto.producto;

public record UpdateProductoRequest(String nombre, Integer stock, long precio, String sucursalId) {}