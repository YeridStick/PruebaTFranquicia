package co.franquicia.model.sucursal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@Builder(toBuilder = true)
public class Sucursal {

    private String id;
    private String franquiciaId;
    private String nombre;
    private Instant createdAt;
    private Instant updatedAt;
}