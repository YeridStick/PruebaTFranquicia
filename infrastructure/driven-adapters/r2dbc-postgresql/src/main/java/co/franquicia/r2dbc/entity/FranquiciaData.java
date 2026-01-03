package co.franquicia.r2dbc.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("franquicia")
public class FranquiciaData {

    @Id
    private String id;

    @Column
    private String nombre;

    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;
}
