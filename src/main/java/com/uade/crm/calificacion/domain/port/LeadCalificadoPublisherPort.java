package com.uade.crm.calificacion.domain.port;

import com.uade.crm.calificacion.domain.model.Calificacion;

public interface LeadCalificadoPublisherPort {
    void publicar(Calificacion calificacion);
}
