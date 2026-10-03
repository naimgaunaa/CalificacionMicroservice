package com.uade.crm.calificacion.domain.reglas;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoRegla {
    private String nombreRegla;
    private int puntos;
    private String descripcion;
    private boolean aplicada;
}
