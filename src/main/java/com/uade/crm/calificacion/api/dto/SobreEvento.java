package com.uade.crm.calificacion.api.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Sobre común de los mensajes de integración (docs/08-contrato-tres-topicos.md del orquestador). Las fechas son texto
 * ISO-8601: así no dependen del ObjectMapper del serializador. {@code destino} no está: solo lo llevan los comandos
 * y calificacion no publica comandos.
 *
 * @param correlationId el del comando que originó el hecho, para que todo el flujo comparta uno
 */
public record SobreEvento<T>(
        String eventId,
        String eventType,
        int version,
        String occurredAt,
        String timestamp,
        String correlationId,
        T payload) {

    /** Clave del MDC con el correlationId del comando que se está atendiendo. */
    public static final String MDC_CORRELATION_ID = "correlationId";

    public static <T> SobreEvento<T> de(String eventType, T payload, String correlationId) {
        String ahora = Instant.now().toString();
        String correlacion = correlationId != null && !correlationId.isBlank() ? correlationId : UUID.randomUUID().toString();
        return new SobreEvento<>(UUID.randomUUID().toString(), eventType, 1, ahora, ahora, correlacion, payload);
    }
}
