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
@Schema(description = "Evento de integración emitido en Kafka cuando un lead ha sido calificado")
public class LeadCalificadoEvent {

    @Schema(description = "ID del Lead", example = "1001")
    private Long leadId;

    @Schema(description = "Puntaje final asignado", example = "60")
    private Integer puntos;

    @Schema(description = "Nivel de calificación", example = "MUY_POTABLE")
    private NivelCalificacion nivel;

    @Schema(description = "Origen de la calificación", example = "AUTOMATICA")
    private OrigenCalificacion origen;

    @Schema(description = "Fecha y hora del evento")
    private LocalDateTime fecha;

    @Schema(description = "Observación o motivo")
    private String observacion;

    @Schema(description = "Resumen de motivos evaluados")
    @Builder.Default
    private List<String> motivos = new ArrayList<>();
}
