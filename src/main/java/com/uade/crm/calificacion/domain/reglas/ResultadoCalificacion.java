package com.uade.crm.calificacion.domain.reglas;

import com.uade.crm.calificacion.domain.model.NivelCalificacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoCalificacion {
    private int puntosTotales;
    private NivelCalificacion nivel;

    @Builder.Default
    private List<ResultadoRegla> resultadosReglas = new ArrayList<>();
}
