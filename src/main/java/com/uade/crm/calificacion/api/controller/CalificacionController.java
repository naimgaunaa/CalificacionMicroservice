package com.uade.crm.calificacion.api.controller;

import com.uade.crm.calificacion.api.dto.CalificacionResponse;
import com.uade.crm.calificacion.api.dto.CorreccionManualRequest;
import com.uade.crm.calificacion.api.dto.MotivoResponse;
import com.uade.crm.calificacion.application.facade.CalificacionFacade;
import com.uade.crm.calificacion.domain.model.Calificacion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/calificaciones")
@RequiredArgsConstructor
@Tag(name = "Calificaciones", description = "Endpoints para consulta y corrección manual de calificaciones de leads")
public class CalificacionController {

    private final CalificacionFacade calificacionFacade;

    @Operation(summary = "Obtener la última calificación de un lead", description = "Retorna el estado de calificación vigente y su desglose de puntos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Calificación encontrada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Lead no encontrado o sin calificación previa",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{leadId}")
    public ResponseEntity<CalificacionResponse> obtenerUltimaCalificacion(
            @Parameter(description = "ID del Lead (UUID o identificador)", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
            @PathVariable String leadId) {
        Calificacion calificacion = calificacionFacade.obtenerUltimaPorLeadId(leadId);
        return ResponseEntity.ok(toResponse(calificacion));
    }

    @Operation(summary = "Obtener historial de calificaciones de un lead", description = "Lista cronológicamente todas las calificaciones calculadas y manuales")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial retornado exitosamente")
    })
    @GetMapping("/{leadId}/historial")
    public ResponseEntity<List<CalificacionResponse>> obtenerHistorial(
            @Parameter(description = "ID del Lead (UUID o identificador)", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
            @PathVariable String leadId) {
        List<Calificacion> historial = calificacionFacade.obtenerHistorialPorLeadId(leadId);
        List<CalificacionResponse> responses = historial.stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Realizar corrección manual de la calificación", description = "Permite a un asesor comercial ajustar manualmente el nivel de potabilidad")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Calificación manual registrada y notificada"),
            @ApiResponse(responseCode = "400", description = "Datos de solicitud inválidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/{leadId}/correcciones")
    public ResponseEntity<CalificacionResponse> realizarCorreccionManual(
            @Parameter(description = "ID del Lead (UUID o identificador)", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
            @PathVariable String leadId,
            @Valid @RequestBody CorreccionManualRequest request) {
        Calificacion calificacion = calificacionFacade.corregirManualmente(leadId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(calificacion));
    }

    private CalificacionResponse toResponse(Calificacion domain) {
        if (domain == null) return null;

        List<MotivoResponse> motivos = domain.getMotivos() != null
                ? domain.getMotivos().stream().map(m -> MotivoResponse.builder()
                        .id(m.getId())
                        .regla(m.getRegla())
                        .puntos(m.getPuntos())
                        .descripcion(m.getDescripcion())
                        .build())
                .toList()
                : Collections.emptyList();

        return CalificacionResponse.builder()
                .id(domain.getId())
                .leadId(domain.getLeadId())
                .clienteId(domain.getClienteId())
                .puntos(domain.getPuntos())
                .nivel(domain.getNivel())
                .origen(domain.getOrigen())
                .fechaCalculo(domain.getFechaCalculo())
                .observacion(domain.getObservacion())
                .motivos(motivos)
                .build();
    }
}
