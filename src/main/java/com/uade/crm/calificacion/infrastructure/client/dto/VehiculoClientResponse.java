package com.uade.crm.calificacion.infrastructure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculoClientResponse {
    private Long id;
    private String marca;
    private String modelo;
    private Integer anio;
    private BigDecimal precio;
    private BigDecimal precioVenta;
    private BigDecimal precioLista;

    public BigDecimal getPrecioEfectivo() {
        if (precio != null) return precio;
        if (precioVenta != null) return precioVenta;
        return precioLista;
    }
}
