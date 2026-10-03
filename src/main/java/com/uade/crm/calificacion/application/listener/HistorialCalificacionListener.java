package com.uade.crm.calificacion.application.listener;

import com.uade.crm.calificacion.domain.event.CalificacionCalculada;
import com.uade.crm.calificacion.domain.model.Calificacion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HistorialCalificacionListener {

    @EventListener
    public void onCalificacionCalculada(CalificacionCalculada event) {
        if (event == null || event.getCalificacion() == null) {
            return;
        }

        Calificacion c = event.getCalificacion();
        log.info("[AUDITORIA DOMINIO] Calificación registrada para leadId: {} | Nivel: {} | Puntos: {} | Origen: {} | Fecha: {}",
                c.getLeadId(),
                c.getNivel(),
                c.getPuntos(),
                c.getOrigen(),
                c.getFechaCalculo());

        if (c.getMotivos() != null && !c.getMotivos().isEmpty()) {
            c.getMotivos().forEach(motivo ->
                    log.debug("  - Regla: {} -> {} ({:+d} pts)", motivo.getRegla(), motivo.getDescripcion(), motivo.getPuntos())
            );
        }
    }
}
