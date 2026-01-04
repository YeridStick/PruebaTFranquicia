package co.franquicia.r2dbc.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Representa un producto en una sucursal.
 * FK: sucursalId -> sucursal.id
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("producto")
public class ProductoData {

    @Id
    private UUID id;

    @Column("sucursal_id")
    private UUID sucursalId;

    @Column
    private String nombre;

    @Column
    private long precio;

    @Column
    private int stock;

    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;
}
