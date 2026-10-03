package com.uade.crm.calificacion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MotivoCalificacion {
    private Long id;
    private String regla;
    private int puntos;
    private String descripcion;
}
