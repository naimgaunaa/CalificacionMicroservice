package com.uade.crm.calificacion.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Evento emitido al completar un preformulario de interés vehicular")
public class PreformularioCompletadoEvent {

    @Schema(description = "ID único del lead (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    private String leadId;

    @Schema(description = "ID del cliente (UUID)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private String clienteId;

    @Schema(description = "ID del vehículo seleccionado (UUID)", example = "7c9e6679-7425-40de-944b-e07fc1f90ae7")
    private String vehiculoId;

    @Schema(description = "Precio del vehículo (si está disponible directamente en el evento)", example = "10000000.00")
    private BigDecimal precioVehiculo;

    @Schema(description = "Monto ofrecido como anticipo en efectivo", example = "3000000.00")
    private BigDecimal montoAnticipo;

    @Schema(description = "Monto cotizado del vehículo usado ofrecido en permuta", example = "3000000.00")
    private BigDecimal montoPermuta;

    @Schema(description = "Indica si la permuta fue categorizada como clavo o no computable", example = "false")
    private boolean permutaNoComputable;

    @Schema(description = "Indica si el interesado requiere financiación", example = "false")
    private boolean solicitaFinanciacion;

    @Schema(description = "Situación crediticia BCRA (si ya viene resuelta en el evento)", example = "1")
    private Integer situacionCrediticia;

    @Schema(description = "Ubicación o provincia del solicitante", example = "BUENOS_AIRES")
    private String ubicacion;

    @Schema(description = "Marca o modelo del vehículo de interés", example = "Toyota Corolla")
    private String vehiculoDescripcion;

    @Schema(description = "Fecha de compleción del preformulario")
    private LocalDateTime timestamp;
}
