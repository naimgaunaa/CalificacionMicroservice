package com.uade.crm.calificacion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatosNegocio {
    private Long leadId;
    private Long vehiculoId;
    private BigDecimal precioVehiculo;
    private BigDecimal montoAnticipo;
    private BigDecimal montoPermuta;
    private boolean permutaNoComputable;
    private boolean solicitaFinanciacion;
    private Integer situacionCrediticia;
    private String ubicacion;

    public BigDecimal getMontoAnticipoSeguro() {
        return montoAnticipo != null ? montoAnticipo : BigDecimal.ZERO;
    }

    public BigDecimal getMontoPermutaSeguro() {
        return montoPermuta != null ? montoPermuta : BigDecimal.ZERO;
    }

    public BigDecimal getPrecioVehiculoSeguro() {
        return precioVehiculo != null ? precioVehiculo : BigDecimal.ZERO;
    }
}
