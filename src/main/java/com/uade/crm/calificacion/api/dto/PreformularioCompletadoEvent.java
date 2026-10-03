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

    @Schema(description = "ID único del lead", example = "1001")
    private Long leadId;

    @Schema(description = "ID del cliente", example = "501")
    private Long clienteId;

    @Schema(description = "ID del vehículo seleccionado", example = "2005")
    private Long vehiculoId;

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
