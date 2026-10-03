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
    private Long clienteId;
    private Integer situacionCrediticia;
    private Integer situacionBcra;
    private String observacion;

    public Integer getSituacionEfectiva() {
        if (situacionCrediticia != null) return situacionCrediticia;
        return situacionBcra;
    }
}
