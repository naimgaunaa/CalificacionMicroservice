package com.uade.crm.calificacion.api.dto;

import com.uade.crm.calificacion.domain.model.NivelCalificacion;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud de corrección manual de calificación")
public class CorreccionManualRequest {

    @Schema(description = "Nuevo puntaje asignado manualmente (opcional si se especifica nivel directo)", example = "75")
    private Integer puntos;

    @NotNull(message = "El nuevo nivel de calificación es obligatorio")
    @Schema(description = "Nuevo nivel asignado manualmente", example = "MUY_POTABLE", requiredMode = Schema.RequiredMode.REQUIRED)
    private NivelCalificacion nuevoNivel;

    @NotBlank(message = "El motivo de la corrección manual es obligatorio")
    @Schema(description = "Explicación o justificación del cambio manual", example = "Cliente VIP con aval bancario adicional", requiredMode = Schema.RequiredMode.REQUIRED)
    private String motivo;

    @Schema(description = "Usuario o asesor comercial que realiza la corrección", example = "asesor_juan_perez")
    private String usuario;
}
