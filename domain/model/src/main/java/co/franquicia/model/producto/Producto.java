package co.franquicia.model.producto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class Producto {

    private String id;
    private String sucursalId;
    private String nombre;
    private long precio;
    private int stock;
    private Instant createdAt;
    private Instant updatedAt;
}
