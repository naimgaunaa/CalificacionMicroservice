package com.uade.crm.calificacion.domain.port;

import com.uade.crm.calificacion.domain.model.Calificacion;

import java.util.List;
import java.util.Optional;

public interface CalificacionPersistencePort {
    Calificacion guardar(Calificacion calificacion);
    Optional<Calificacion> buscarUltimaPorLeadId(String leadId);
    List<Calificacion> buscarHistorialPorLeadId(String leadId);
}
