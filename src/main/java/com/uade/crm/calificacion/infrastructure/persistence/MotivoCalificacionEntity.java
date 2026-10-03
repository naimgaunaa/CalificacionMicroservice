package com.uade.crm.calificacion.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "motivos_calificacion")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MotivoCalificacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String regla;

    @Column(nullable = false)
    private Integer puntos;

    @Column(nullable = false, length = 500)
    private String descripcion;
}
