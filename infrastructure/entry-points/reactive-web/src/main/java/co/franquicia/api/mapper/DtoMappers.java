package co.franquicia.api.mapper;


import co.franquicia.api.dto.producto.ProductoViewDTO;

import java.util.Map;

public final class DtoMappers {
    private DtoMappers() {}

    public static ProductoViewDTO toProductoViewDTO(Map<String, Object> m) {
        String productoId       = (String) m.get("productoId");
        String productoNombre   = (String) m.get("productoNombre");
        int stock               = toInt(m.get("stock"), 0);
        long precio             = (Long) m.get("precio");

        String franquiciaId     = (String) m.get("franquiciaId");
        String franquiciaNombre = (String) m.get("franquiciaNombre");
        String sucursalId       = (String) m.get("sucursalId");
        String sucursalNombre   = (String) m.get("sucursalNombre");

        return new ProductoViewDTO(
                productoId, productoNombre, stock, precio,
                franquiciaId, franquiciaNombre,
                sucursalId, sucursalNombre
        );
    }

    private static int toInt(Object val, int def) {
        if (val == null) return def;
        if (val instanceof Number n) return n.intValue();
        try { return Integer.parseInt(val.toString()); }
        catch (Exception e) { return def; }
    }
}