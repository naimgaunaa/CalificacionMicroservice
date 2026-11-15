package com.uade.crm.calificacion.api.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uade.crm.calificacion.api.dto.PreformularioCompletadoEvent;
import com.uade.crm.calificacion.api.dto.SobreEvento;
import com.uade.crm.calificacion.application.facade.CalificacionFacade;
import com.uade.crm.calificacion.domain.model.Calificacion;
import com.uade.crm.calificacion.domain.model.NivelCalificacion;
import com.uade.crm.calificacion.domain.model.OrigenCalificacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Calificacion atiende solo el comando {@code CalificarLead} con {@code destino: calificacion}, en
 * {@code crm.comandos} (contrato: docs/08-contrato-tres-topicos.md del orquestador).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Listener del comando CalificarLead")
class CalificarLeadListenerTest {

    private static final String PAYLOAD = """
            {"leadId":"L1","clienteId":"C1","vehiculoId":"V1","montoAnticipo":1500000.0,"montoPermuta":0,
             "permutaNoComputable":false,"solicitaFinanciacion":true,"ubicacion":"Córdoba"}""";

    @Mock
    private CalificacionFacade facade;

    private CalificarLeadListener listener;

    @BeforeEach
    void setUp() {
        listener = new CalificarLeadListener(facade, new ObjectMapper());
        MDC.clear();
    }

    private static String comando(String eventType, String destino) {
        return """
                {"eventId":"e1","eventType":"%s","version":1,"correlationId":"cid-1","destino":%s,"payload":%s}"""
                .formatted(eventType, destino == null ? "null" : "\"" + destino + "\"", PAYLOAD);
    }

    private static Calificacion calificada() {
        return Calificacion.builder().leadId("L1").nivel(NivelCalificacion.POTABLE)
                .origen(OrigenCalificacion.AUTOMATICA).build();
    }

    @Test
    @DisplayName("califica el lead con los datos del payload")
    void calificaElLead() {
        when(facade.calificarLead(any())).thenReturn(calificada());

        listener.escucharComando(comando("CalificarLead", "calificacion"));

        ArgumentCaptor<PreformularioCompletadoEvent> captor = ArgumentCaptor.forClass(PreformularioCompletadoEvent.class);
        verify(facade).calificarLead(captor.capture());
        PreformularioCompletadoEvent evento = captor.getValue();
        assertThat(evento.getLeadId()).isEqualTo("L1");
        assertThat(evento.getClienteId()).isEqualTo("C1");
        assertThat(evento.getVehiculoId()).isEqualTo("V1");
        assertThat(evento.getMontoAnticipo()).isEqualByComparingTo(new BigDecimal("1500000.0"));
        assertThat(evento.isSolicitaFinanciacion()).isTrue();
        assertThat(evento.getUbicacion()).isEqualTo("Córdoba");
    }

    @Test
    @DisplayName("mientras califica, el correlationId del comando está en el MDC (lo usa el publicador) y después se limpia")
    void propagaElCorrelationId() {
        AtomicReference<String> visto = new AtomicReference<>();
        when(facade.calificarLead(any())).thenAnswer(invocacion -> {
            visto.set(MDC.get(SobreEvento.MDC_CORRELATION_ID));
            return calificada();
        });

        listener.escucharComando(comando("CalificarLead", "calificacion"));

        assertThat(visto.get()).isEqualTo("cid-1");
        assertThat(MDC.get(SobreEvento.MDC_CORRELATION_ID)).isNull();
    }

    @Test
    @DisplayName("ignora los comandos de otros servicios")
    void ignoraOtrosDestinos() {
        listener.escucharComando(comando("RegistrarCalificacion", "clientes"));
        listener.escucharComando(comando("CalificarLead", "leads"));
        listener.escucharComando(comando("CalificarLead", null));

        verifyNoInteractions(facade);
    }

    @Test
    @DisplayName("ignora un comando desconocido para calificacion")
    void ignoraComandosDesconocidos() {
        listener.escucharComando(comando("OtraCosa", "calificacion"));

        verifyNoInteractions(facade);
    }

    @Test
    @DisplayName("ignora un mensaje sin sobre (el formato anterior)")
    void ignoraMensajesSinSobre() {
        listener.escucharComando(PAYLOAD);

        verifyNoInteractions(facade);
    }

    @Test
    @DisplayName("omite el comando sin leadId")
    void omiteSinLeadId() {
        listener.escucharComando("""
                {"eventType":"CalificarLead","destino":"calificacion","payload":{"clienteId":"C1"}}""");

        verify(facade, never()).calificarLead(any());
    }

    @Test
    @DisplayName("si no se puede interpretar o falla la calificación, lanza la excepción (reintentos y DLT)")
    void laFallaSePropaga() {
        assertThatThrownBy(() -> listener.escucharComando("{no es json"))
                .isInstanceOf(RuntimeException.class);

        when(facade.calificarLead(any())).thenThrow(new IllegalStateException("base caída"));
        assertThatThrownBy(() -> listener.escucharComando(comando("CalificarLead", "calificacion")))
                .isInstanceOf(RuntimeException.class);
        assertThat(MDC.get(SobreEvento.MDC_CORRELATION_ID)).isNull();
    }
}
