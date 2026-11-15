package com.uade.crm.calificacion.infrastructure.messaging;

import com.uade.crm.calificacion.api.dto.LeadCalificadoEvent;
import com.uade.crm.calificacion.api.dto.SobreEvento;
import com.uade.crm.calificacion.domain.model.Calificacion;
import com.uade.crm.calificacion.domain.port.LeadCalificadoPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Publica el hecho {@code LeadCalificado} en {@code crm.eventos}, dentro del sobre común. Lo lee el orquestador, que
 * decide qué sigue (registrar la calificación en clientes y leads, pedir el resumen). El {@code correlationId} es el
 * del comando que se estaba atendiendo (ver {@code CalificarLeadListener}); en una corrección manual por REST no hay
 * comando de origen y se genera uno nuevo.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LeadCalificadoPublisher implements LeadCalificadoPublisherPort {

    static final String TIPO_EVENTO = "LeadCalificado";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${crm.kafka.topicos.eventos:crm.eventos}")
    private String topicoEventos;

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

        SobreEvento<LeadCalificadoEvent> sobre =
                SobreEvento.de(TIPO_EVENTO, event, MDC.get(SobreEvento.MDC_CORRELATION_ID));
        String key = calificacion.getLeadId();

        kafkaTemplate.send(topicoEventos, key, sobre)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Evento LeadCalificado publicado exitosamente en {} [leadId: {}, clienteId: {}, nivel: {}, puntos: {}]",
                                topicoEventos, event.getLeadId(), event.getClienteId(), event.getNivel(), event.getPuntos());
                    } else {
                        log.error("Fallo al publicar evento LeadCalificado en {} [leadId: {}]: {}",
                                topicoEventos, event.getLeadId(), ex.getMessage());
                    }
                });
    }
}
