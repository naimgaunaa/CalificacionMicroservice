package com.uade.crm.calificacion.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detalle del motivo o regla de calificación")
public class MotivoResponse {

    @Schema(description = "ID del motivo", example = "1")
    private Long id;

    @Schema(description = "Nombre de la regla evaluada", example = "REGLA_COBERTURA")
    private String regla;

    @Schema(description = "Puntos otorgados o deducidos por la regla", example = "60")
    private Integer puntos;

    @Schema(description = "Descripción detallada del resultado de la regla", example = "Cobertura aportada: 60.00% (>= 50% (+60 pts))")
    private String descripcion;
}
