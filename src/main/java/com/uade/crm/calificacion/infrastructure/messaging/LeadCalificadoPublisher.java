package com.uade.crm.calificacion.infrastructure.messaging;

import com.uade.crm.calificacion.api.dto.LeadCalificadoEvent;
import com.uade.crm.calificacion.domain.model.Calificacion;
import com.uade.crm.calificacion.domain.port.LeadCalificadoPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeadCalificadoPublisher implements LeadCalificadoPublisherPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${crm.kafka.topicos.lead-calificado:lead-calificado}")
    private String topicoLeadCalificado;

    @Override
    public void publicar(Calificacion calificacion) {
        if (calificacion == null || calificacion.getLeadId() == null) {
            log.warn("Se intentó publicar una calificación nula o sin leadId");
            return;
        }

        List<String> motivosTexto = calificacion.getMotivos() != null
                ? calificacion.getMotivos().stream().map(m -> String.format("[%s]: %s (%+d pts)", m.getRegla(), m.getDescripcion(), m.getPuntos())).toList()
                : Collections.emptyList();

        LeadCalificadoEvent event = LeadCalificadoEvent.builder()
                .leadId(calificacion.getLeadId())
                .clienteId(calificacion.getClienteId())
                .puntos(calificacion.getPuntos())
                .nivel(calificacion.getNivel())
                .origen(calificacion.getOrigen())
                .fecha(calificacion.getFechaCalculo() != null ? calificacion.getFechaCalculo() : LocalDateTime.now())
                .observacion(calificacion.getObservacion())
                .motivos(motivosTexto)
                .build();

        String key = calificacion.getLeadId();

        kafkaTemplate.send(topicoLeadCalificado, key, event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Evento LeadCalificado publicado exitosamente en {} [leadId: {}, clienteId: {}, nivel: {}, puntos: {}]",
                                topicoLeadCalificado, event.getLeadId(), event.getClienteId(), event.getNivel(), event.getPuntos());
                    } else {
                        log.error("Fallo al publicar evento LeadCalificado en {} [leadId: {}]: {}",
                                topicoLeadCalificado, event.getLeadId(), ex.getMessage());
                    }
                });
    }
}
