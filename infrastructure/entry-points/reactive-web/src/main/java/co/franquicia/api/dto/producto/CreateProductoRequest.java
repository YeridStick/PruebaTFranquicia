package co.franquicia.api.dto.producto;

public record CreateProductoRequest(
        String nombre,
        int stock,
        long precio
) { }
