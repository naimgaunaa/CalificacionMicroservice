package com.uade.crm.calificacion.domain.reglas;

import com.uade.crm.calificacion.domain.model.DatosNegocio;

public interface ReglaCalificacion {
    ResultadoRegla evaluar(DatosNegocio datos);
}
