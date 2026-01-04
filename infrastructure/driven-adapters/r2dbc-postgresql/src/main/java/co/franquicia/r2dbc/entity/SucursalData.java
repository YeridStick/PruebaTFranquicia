package co.franquicia.r2dbc.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Representa una sucursal de una franquicia.
 * FK: franquiciaId -> franquicia.id
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("sucursal")
public class SucursalData {

    @Id
    private UUID id;

    @Column("franquicia_id")
    private UUID franquiciaId;

    @Column
    private String nombre;

    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;
}
