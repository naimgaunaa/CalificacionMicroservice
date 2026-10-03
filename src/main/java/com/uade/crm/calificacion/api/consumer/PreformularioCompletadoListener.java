package com.uade.crm.calificacion.api.consumer;

import com.uade.crm.calificacion.api.dto.PreformularioCompletadoEvent;
import com.uade.crm.calificacion.application.facade.CalificacionFacade;
import com.uade.crm.calificacion.domain.model.Calificacion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PreformularioCompletadoListener {

    private final CalificacionFacade calificacionFacade;

    @KafkaListener(
            topics = "${crm.kafka.topicos.preformulario-completado:preformulario-completado}",
            groupId = "${spring.kafka.consumer.group-id:calificacion-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void escucharPreformulario(@Payload PreformularioCompletadoEvent event) {
        log.info("Recibido evento de preformulario completado para leadId: {}", event != null ? event.getLeadId() : null);

        if (event == null || event.getLeadId() == null) {
            log.warn("Evento de preformulario nulo o sin leadId recibido. Se omite el procesamiento.");
            return;
        }

        try {
            Calificacion calificacion = calificacionFacade.calificarLead(event);
            log.info("Procesamiento completado con éxito para leadId: {} -> Nivel: {}, Puntos: {}",
                    event.getLeadId(), calificacion.getNivel(), calificacion.getPuntos());
        } catch (Exception ex) {
            log.error("Error al procesar el evento de preformulario completado para leadId: {}: {}",
                    event.getLeadId(), ex.getMessage(), ex);
            throw ex; // Permite que el ErrorHandler de Kafka reintente o mande a DLT
        }
    }
}
