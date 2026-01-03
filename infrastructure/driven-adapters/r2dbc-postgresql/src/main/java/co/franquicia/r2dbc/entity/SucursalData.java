package co.franquicia.r2dbc.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

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
    private String id;

    @Column("franquicia_id")
    private String franquiciaId;

    @Column
    private String nombre;

    @Column
    private Instant createdAt;

    @Column
    private Instant updatedAt;
}