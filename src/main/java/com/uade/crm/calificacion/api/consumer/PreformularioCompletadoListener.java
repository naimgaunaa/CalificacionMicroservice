package com.uade.crm.calificacion.api.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uade.crm.calificacion.api.dto.PreformularioCompletadoEvent;
import com.uade.crm.calificacion.application.facade.CalificacionFacade;
import com.uade.crm.calificacion.domain.model.Calificacion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class PreformularioCompletadoListener {

    private final CalificacionFacade calificacionFacade;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${crm.kafka.topicos.preformulario-completado:preformulario-completado}",
            groupId = "${spring.kafka.consumer.group-id:calificacion-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void escucharPreformulario(String mensaje) {
        if (mensaje == null || mensaje.isBlank()) {
            log.warn("Evento de preformulario vacío recibido. Se omite el procesamiento.");
            return;
        }

        PreformularioCompletadoEvent event;
        try {
            event = parse(mensaje);
        } catch (Exception ex) {
            log.error("No se pudo interpretar el mensaje de preformulario-completado: {}", ex.getMessage(), ex);
            throw ex; // reintenta o va a DLT
        }

        if (event.getLeadId() == null) {
            log.warn("Evento de preformulario sin leadId recibido. Se omite el procesamiento.");
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

    /**
     * Acepta el mensaje envuelto en el sobre común ({@code payload}) o plano, tal
     * como lo publica leads. Los montos pueden venir como número; se tolera el campo
     * {@code anticipo} además de {@code montoAnticipo}.
     */
    private PreformularioCompletadoEvent parse(String mensaje) throws Exception {
        JsonNode raiz = objectMapper.readTree(mensaje);
        JsonNode datos = raiz.path("payload").isObject() ? raiz.get("payload") : raiz;

        BigDecimal anticipo = decimal(datos, "montoAnticipo", "anticipo");
        BigDecimal permuta = decimal(datos, "montoPermuta", "valorPermutaDeclarado");
        boolean noComputable = datos.path("permutaNoComputable").asBoolean(false);
        boolean solicitaFinanciacion = datos.path("solicitaFinanciacion").asBoolean(false);
        String ubicacion = datos.path("ubicacion").isTextual() ? datos.get("ubicacion").asText()
                : (datos.path("provincia").isTextual() ? datos.get("provincia").asText() : null);

        return PreformularioCompletadoEvent.builder()
                .leadId(texto(datos, "leadId"))
                .clienteId(texto(datos, "clienteId"))
                .vehiculoId(texto(datos, "vehiculoId"))
                .montoAnticipo(anticipo)
                .montoPermuta(permuta)
                .permutaNoComputable(noComputable)
                .solicitaFinanciacion(solicitaFinanciacion)
                .ubicacion(ubicacion)
                .build();
    }

    private static String texto(JsonNode datos, String campo) {
        JsonNode nodo = datos.get(campo);
        return nodo != null && nodo.isTextual() ? nodo.asText() : null;
    }

    private static BigDecimal decimal(JsonNode datos, String... campos) {
        for (String campo : campos) {
            JsonNode nodo = datos.get(campo);
            if (nodo != null && nodo.isNumber()) {
                return nodo.decimalValue();
            }
        }
        return null;
    }
}
