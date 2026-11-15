package com.uade.crm.calificacion.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @Schema(description = "ID del Lead (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    private String leadId;

    @Schema(description = "ID del Cliente (UUID) asociado para registrar en su historial", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private String clienteId;

    @Schema(description = "Puntaje final asignado", example = "60")
    private Integer puntos;

    @Schema(description = "Nivel de calificación", example = "MUY_POTABLE")
    private NivelCalificacion nivel;

    @Schema(description = "Origen de la calificación", example = "AUTOMATICA")
    private OrigenCalificacion origen;

    // STRING fuerza ISO-8601 ("2026-11-14T18:56:06.049848900"). Sin esto, el JsonSerializer de Spring Kafka
    // escribe el LocalDateTime como array ([2026,11,14,18,56,6,...]) y clientes lo rechaza (va a su DLT).
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Schema(description = "Fecha y hora del evento (ISO-8601, sin zona)", example = "2026-10-11T15:30:00")
    private LocalDateTime fecha;

    @Schema(description = "Observación o motivo")
    private String observacion;

    @Schema(description = "Resumen de motivos evaluados")
    @Builder.Default
    private List<String> motivos = new ArrayList<>();
}
