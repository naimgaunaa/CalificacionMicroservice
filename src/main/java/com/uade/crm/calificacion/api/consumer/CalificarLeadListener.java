package com.uade.crm.calificacion.api.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uade.crm.calificacion.api.dto.PreformularioCompletadoEvent;
import com.uade.crm.calificacion.api.dto.SobreEvento;
import com.uade.crm.calificacion.application.facade.CalificacionFacade;
import com.uade.crm.calificacion.domain.model.Calificacion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Atiende el comando {@code CalificarLead} que publica el orquestador en {@code crm.comandos}
 * (contrato: docs/08-contrato-tres-topicos.md del orquestador). Los comandos cuyo {@code destino} no es
 * {@code calificacion} se ignoran: todos los servicios leen el mismo tópico.
 *
 * <p>El payload es el del evento {@code PreformularioCompletado} que publicó leads. Mientras califica, deja el
 * {@code correlationId} del comando en el MDC para que el evento {@code LeadCalificado} salga con el mismo.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CalificarLeadListener {

    static final String SERVICIO = "calificacion";
    static final String COMANDO = "CalificarLead";

    private final CalificacionFacade calificacionFacade;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${crm.kafka.topicos.comandos:crm.comandos}",
            groupId = "${spring.kafka.consumer.group-id:calificacion-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void escucharComando(String mensaje) {
        if (mensaje == null || mensaje.isBlank()) {
            log.warn("Mensaje vacío recibido en el tópico de comandos. Se omite el procesamiento.");
            return;
        }

        JsonNode raiz;
        try {
            raiz = objectMapper.readTree(mensaje);
        } catch (Exception ex) {
            log.error("No se pudo interpretar el comando: {}", ex.getMessage(), ex);
            throw new RuntimeException("No se pudo interpretar el comando", ex); // reintenta o va a DLT
        }

        if (!SERVICIO.equals(raiz.path("destino").asText(null))) {
            return; // es para otro servicio (o no tiene sobre): no es asunto de calificacion
        }
        String tipo = raiz.path("eventType").asText(null);
        if (!COMANDO.equals(tipo)) {
            log.warn("Comando desconocido para calificacion: {}", tipo);
            return;
        }

        PreformularioCompletadoEvent event;
        try {
            event = parse(raiz);
        } catch (Exception ex) {
            log.error("No se pudo interpretar el payload de {}: {}", COMANDO, ex.getMessage(), ex);
            throw new RuntimeException("No se pudo interpretar el payload de " + COMANDO, ex);
        }

        if (event.getLeadId() == null) {
            log.warn("Comando {} sin leadId recibido. Se omite el procesamiento.", COMANDO);
            return;
        }

        String correlationId = raiz.path("correlationId").asText(null);
        if (correlationId != null) {
            MDC.put(SobreEvento.MDC_CORRELATION_ID, correlationId);
        }
        try {
            Calificacion calificacion = calificacionFacade.calificarLead(event);
            log.info("Procesamiento completado con éxito para leadId: {} -> Nivel: {}, Puntos: {}",
                    event.getLeadId(), calificacion.getNivel(), calificacion.getPuntos());
        } catch (Exception ex) {
            log.error("Error al calificar el leadId: {}: {}", event.getLeadId(), ex.getMessage(), ex);
            throw new RuntimeException("Error al procesar el comando " + COMANDO, ex); // reintenta o va a DLT
        } finally {
            MDC.remove(SobreEvento.MDC_CORRELATION_ID);
        }
    }

    /**
     * Lee los datos del payload del sobre. Los montos pueden venir como número; se tolera el campo
     * {@code anticipo} además de {@code montoAnticipo}.
     */
    private PreformularioCompletadoEvent parse(JsonNode raiz) {
        JsonNode datos = raiz.path("payload");

        BigDecimal anticipo = decimal(datos, "montoAnticipo", "anticipo");
        BigDecimal permuta = decimal(datos, "montoPermuta", "valorPermutaDeclarado");
        boolean noComputable = datos.path("permutaNoComputable").asBoolean(false);
        boolean solicitaFinanciacion = datos.path("solicitaFinanciacion").asBoolean(false);
        String ubicacion = datos.path("ubicacion").isTextual() ? datos.get("ubicacion").asText()
                : (datos.path("provincia").isTextual() ? datos.get("provincia").asText() : null);
        String patente = datos.path("patente").isTextual() ? datos.get("patente").asText() : null;

        return PreformularioCompletadoEvent.builder()
                .leadId(texto(datos, "leadId"))
                .clienteId(texto(datos, "clienteId"))
                .vehiculoId(texto(datos, "vehiculoId"))
                .montoAnticipo(anticipo)
                .montoPermuta(permuta)
                .permutaNoComputable(noComputable)
                .solicitaFinanciacion(solicitaFinanciacion)
                .ubicacion(ubicacion)
                .patente(patente)
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
