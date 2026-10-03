package com.uade.crm.calificacion.api.dto;

import com.uade.crm.calificacion.domain.model.NivelCalificacion;
import com.uade.crm.calificacion.domain.model.OrigenCalificacion;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Respuesta con los datos de la calificación del lead")
public class CalificacionResponse {

    @Schema(description = "ID de la calificación", example = "10")
    private Long id;

    @Schema(description = "ID del Lead calificado", example = "1001")
    private Long leadId;

    @Schema(description = "Puntaje total obtenido", example = "60")
    private Integer puntos;

    @Schema(description = "Nivel de calificación resultante", example = "MUY_POTABLE")
    private NivelCalificacion nivel;

    @Schema(description = "Origen de la calificación", example = "AUTOMATICA")
    private OrigenCalificacion origen;

    @Schema(description = "Fecha y hora del cálculo")
    private LocalDateTime fechaCalculo;

    @Schema(description = "Observaciones o notas adicionales", example = "Calificación automática basada en preformulario")
    private String observacion;

    @Schema(description = "Desglose de motivos y reglas aplicadas")
    @Builder.Default
    private List<MotivoResponse> motivos = new ArrayList<>();
}
