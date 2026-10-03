package com.uade.crm.calificacion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Calificacion {
    private Long id;
    private Long leadId;
    private int puntos;
    private NivelCalificacion nivel;
    private OrigenCalificacion origen;
    private LocalDateTime fechaCalculo;
    private String observacion;

    @Builder.Default
    private List<MotivoCalificacion> motivos = new ArrayList<>();
}
