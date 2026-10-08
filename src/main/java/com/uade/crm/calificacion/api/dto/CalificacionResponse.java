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

    @Schema(description = "ID interno de la calificación", example = "10")
    private Long id;

    @Schema(description = "ID del Lead calificado (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    private String leadId;

    @Schema(description = "ID del Cliente (UUID)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private String clienteId;

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
