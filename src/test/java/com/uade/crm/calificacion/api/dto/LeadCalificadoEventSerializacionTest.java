package com.uade.crm.calificacion.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uade.crm.calificacion.domain.model.NivelCalificacion;
import com.uade.crm.calificacion.domain.model.OrigenCalificacion;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * El productor usa el JsonSerializer de Spring Kafka (ver application.yml). Con su ObjectMapper por defecto un
 * LocalDateTime sale como array; clientes (y el contrato) esperan texto ISO-8601.
 */
class LeadCalificadoEventSerializacionTest {

    @Test
    void laFechaSeSerializaComoTextoIso8601() throws Exception {
        LeadCalificadoEvent evento = LeadCalificadoEvent.builder()
                .leadId("550e8400-e29b-41d4-a716-446655440000")
                .clienteId("3fa85f64-5717-4562-b3fc-2c963f66afa6")
                .puntos(60)
                .nivel(NivelCalificacion.MUY_POTABLE)
                .origen(OrigenCalificacion.AUTOMATICA)
                .fecha(LocalDateTime.of(2026, 11, 14, 18, 56, 6, 49_848_900))
                .build();

        byte[] bytes;
        try (JsonSerializer<LeadCalificadoEvent> serializador = new JsonSerializer<>()) {
            bytes = serializador.serialize("lead-calificado", evento);
        }

        JsonNode fecha = new ObjectMapper().readTree(bytes).get("fecha");
        assertThat(fecha.isTextual()).as("fecha debe ser texto, no array: %s", fecha).isTrue();
        assertThat(LocalDateTime.parse(fecha.asText())).isEqualTo(evento.getFecha());
    }
}
