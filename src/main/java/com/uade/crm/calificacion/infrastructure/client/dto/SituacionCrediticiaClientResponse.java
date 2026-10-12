package com.uade.crm.calificacion.infrastructure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SituacionCrediticiaClientResponse {
    private String clienteId;
    private String estado;
    private Integer situacion;
    private Integer situacionCrediticia;
    private Integer situacionBcra;
    private String observacion;

    public Integer getSituacionEfectiva() {
        if (situacion != null) return situacion;
        if (situacionCrediticia != null) return situacionCrediticia;
        return situacionBcra;
    }
}
