package com.uade.crm.calificacion.infrastructure.persistence;

import com.uade.crm.calificacion.domain.model.NivelCalificacion;
import com.uade.crm.calificacion.domain.model.OrigenCalificacion;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "calificaciones", indexes = {
        @Index(name = "idx_calificaciones_lead_id", columnList = "lead_id"),
        @Index(name = "idx_calificaciones_fecha", columnList = "fecha_calculo")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalificacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lead_id", nullable = false)
    private Long leadId;

    @Column(nullable = false)
    private Integer puntos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NivelCalificacion nivel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrigenCalificacion origen;

    @Column(name = "fecha_calculo", nullable = false)
    private LocalDateTime fechaCalculo;

    @Column(length = 1000)
    private String observacion;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "calificacion_id")
    @Builder.Default
    private List<MotivoCalificacionEntity> motivos = new ArrayList<>();
}
