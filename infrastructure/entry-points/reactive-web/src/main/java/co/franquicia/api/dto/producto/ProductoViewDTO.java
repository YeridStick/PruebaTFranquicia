package co.franquicia.api.dto.producto;

public record ProductoViewDTO(
        String productoId,
        String productoNombre,
        int stock,
        long precio,
        String franquiciaId,
        String franquiciaNombre,
        String sucursalId,
        String sucursalNombre
) {}
