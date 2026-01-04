package co.franquicia.api.mapper;

import co.franquicia.api.dto.producto.ProductoViewDTO;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DtoMappersTest {

    @Test
    void toProductoViewDTO_mapsFields() {
        Map<String, Object> m = new HashMap<>();
        m.put("productoId", "p1");
        m.put("productoNombre", "Prod");
        m.put("stock", 5);
        m.put("precio", 100L);
        m.put("franquiciaId", "f1");
        m.put("franquiciaNombre", "Franq");
        m.put("sucursalId", "s1");
        m.put("sucursalNombre", "Suc");

        ProductoViewDTO dto = DtoMappers.toProductoViewDTO(m);

        assertThat(dto.productoId()).isEqualTo("p1");
        assertThat(dto.productoNombre()).isEqualTo("Prod");
        assertThat(dto.stock()).isEqualTo(5);
        assertThat(dto.precio()).isEqualTo(100L);
        assertThat(dto.franquiciaId()).isEqualTo("f1");
        assertThat(dto.franquiciaNombre()).isEqualTo("Franq");
        assertThat(dto.sucursalId()).isEqualTo("s1");
        assertThat(dto.sucursalNombre()).isEqualTo("Suc");
    }

    @Test
    void toProductoViewDTO_parsesStockFromString() {
        Map<String, Object> m = new HashMap<>();
        m.put("productoId", "p1");
        m.put("productoNombre", "Prod");
        m.put("stock", "7");
        m.put("precio", 100L);
        m.put("franquiciaId", "f1");
        m.put("franquiciaNombre", "Franq");
        m.put("sucursalId", "s1");
        m.put("sucursalNombre", "Suc");

        ProductoViewDTO dto = DtoMappers.toProductoViewDTO(m);
        assertThat(dto.stock()).isEqualTo(7);
    }

    @Test
    void toProductoViewDTO_defaultsStockWhenNullOrInvalid() {
        Map<String, Object> m = new HashMap<>();
        m.put("productoId", "p1");
        m.put("productoNombre", "Prod");
        m.put("precio", 100L);
        m.put("franquiciaId", "f1");
        m.put("franquiciaNombre", "Franq");
        m.put("sucursalId", "s1");
        m.put("sucursalNombre", "Suc");

        // stock ausente -> 0
        ProductoViewDTO dtoNull = DtoMappers.toProductoViewDTO(m);
        assertThat(dtoNull.stock()).isEqualTo(0);

        // stock inválido -> 0
        m.put("stock", "no-num");
        ProductoViewDTO dtoInvalid = DtoMappers.toProductoViewDTO(m);
        assertThat(dtoInvalid.stock()).isEqualTo(0);
    }
}
