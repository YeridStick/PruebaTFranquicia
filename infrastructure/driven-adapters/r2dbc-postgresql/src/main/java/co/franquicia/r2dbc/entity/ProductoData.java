package co.franquicia.r2dbc.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

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
    private String id;

    @Column("sucursal_id")
    private String sucursalId;

    @Column
    private String nombre;

    @Column
    private long precio;

    @Column
    private int stock;

    @Column
    private Instant createdAt;

    @Column
    private Instant updatedAt;
}