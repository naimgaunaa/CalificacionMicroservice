package com.uade.crm.calificacion.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uade.crm.calificacion.api.dto.LeadCalificadoEvent;
import com.uade.crm.calificacion.api.dto.SobreEvento;
import com.uade.crm.calificacion.domain.model.Calificacion;
import com.uade.crm.calificacion.domain.model.NivelCalificacion;
import com.uade.crm.calificacion.domain.model.OrigenCalificacion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Calificacion publica el hecho {@code LeadCalificado} en {@code crm.eventos}, dentro del sobre común
 * (contrato: docs/08-contrato-tres-topicos.md del orquestador).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Publicador del evento LeadCalificado")
class LeadCalificadoPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafka;

    private LeadCalificadoPublisher publicador;

    @BeforeEach
    void setUp() {
        publicador = new LeadCalificadoPublisher(kafka);
        ReflectionTestUtils.setField(publicador, "topicoEventos", "crm.eventos");
        MDC.clear();
    }

    @AfterEach
    void limpiar() {
        MDC.clear();
    }

    private static Calificacion calificacion() {
        return Calificacion.builder()
                .leadId("L1").clienteId("C1").puntos(70)
                .nivel(NivelCalificacion.POTABLE).origen(OrigenCalificacion.AUTOMATICA)
                .fechaCalculo(LocalDateTime.of(2026, 11, 15, 12, 0, 1))
                .build();
    }

    @SuppressWarnings("unchecked")
    private SobreEvento<LeadCalificadoEvent> publicar(Calificacion calificacion) {
        when(kafka.send(eq("crm.eventos"), any(String.class), any()))
                .thenReturn(new CompletableFuture<SendResult<String, Object>>());
        publicador.publicar(calificacion);
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(kafka).send(eq("crm.eventos"), eq(calificacion.getLeadId()), captor.capture());
        return (SobreEvento<LeadCalificadoEvent>) captor.getValue();
    }

    @Test
    @DisplayName("publica en crm.eventos, con la clave leadId y el payload de siempre dentro del sobre")
    void publicaEnElSobreComun() {
        SobreEvento<LeadCalificadoEvent> sobre = publicar(calificacion());

        assertThat(sobre.eventType()).isEqualTo("LeadCalificado");
        assertThat(sobre.version()).isEqualTo(1);
        assertThat(sobre.eventId()).isNotBlank();
        assertThat(sobre.occurredAt()).isEqualTo(sobre.timestamp());
        LeadCalificadoEvent payload = sobre.payload();
        assertThat(payload.getLeadId()).isEqualTo("L1");
        assertThat(payload.getClienteId()).isEqualTo("C1");
        assertThat(payload.getPuntos()).isEqualTo(70);
        assertThat(payload.getNivel()).isEqualTo(NivelCalificacion.POTABLE);
        assertThat(payload.getOrigen()).isEqualTo(OrigenCalificacion.AUTOMATICA);
    }

    @Test
    @DisplayName("usa el correlationId del comando que originó la calificación")
    void usaElCorrelationIdDelMdc() {
        MDC.put(SobreEvento.MDC_CORRELATION_ID, "cid-del-comando");

        assertThat(publicar(calificacion()).correlationId()).isEqualTo("cid-del-comando");
    }

    @Test
    @DisplayName("sin comando de origen (corrección manual por REST) genera un correlationId nuevo")
    void generaCorrelationIdSiNoHay() {
        String correlationId = publicar(calificacion()).correlationId();

        assertThat(UUID.fromString(correlationId)).isNotNull();
    }

    @Test
    @DisplayName("el sobre serializa las fechas como texto ISO-8601 y deja el payload anidado")
    void serializaComoJsonCompleto() throws Exception {
        SobreEvento<LeadCalificadoEvent> sobre = publicar(calificacion());

        byte[] bytes;
        try (JsonSerializer<Object> serializador = new JsonSerializer<>()) {
            bytes = serializador.serialize("crm.eventos", sobre);
        }
        JsonNode json = new ObjectMapper().readTree(bytes);

        assertThat(json.get("eventType").asText()).isEqualTo("LeadCalificado");
        assertThat(json.get("occurredAt").isTextual()).isTrue();
        assertThat(json.has("destino")).as("solo los comandos llevan destino").isFalse();
        JsonNode payload = json.get("payload");
        assertThat(payload.get("fecha").isTextual()).as("fecha como texto, no array: %s", payload.get("fecha")).isTrue();
        assertThat(LocalDateTime.parse(payload.get("fecha").asText())).isEqualTo(LocalDateTime.of(2026, 11, 15, 12, 0, 1));
        assertThat(payload.get("origen").asText()).isEqualTo("AUTOMATICA");
        assertThat(payload.get("motivos").isArray()).isTrue();
    }

    @Test
    @DisplayName("no publica una calificación sin leadId")
    void noPublicaSinLeadId() {
        publicador.publicar(Calificacion.builder().build());
        publicador.publicar(null);

        verifyNoInteractions(kafka);
    }
}
