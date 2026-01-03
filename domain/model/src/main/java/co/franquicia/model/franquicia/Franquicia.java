package co.franquicia.model.franquicia;

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
public class Franquicia {

    private String id;
    private String nombre;
    private Instant createdAt;
    private Instant updatedAt;
}
