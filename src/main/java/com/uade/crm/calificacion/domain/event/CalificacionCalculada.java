package com.uade.crm.calificacion.domain.event;

import com.uade.crm.calificacion.domain.model.Calificacion;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CalificacionCalculada {

    private final Long leadId;
    private final Calificacion calificacion;
    private final LocalDateTime fecha;

    public CalificacionCalculada(Calificacion calificacion) {
        this.calificacion = calificacion;
        this.leadId = calificacion != null ? calificacion.getLeadId() : null;
        this.fecha = LocalDateTime.now();
    }
}
