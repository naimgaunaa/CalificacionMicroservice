package com.uade.crm.calificacion.application;

import com.uade.crm.calificacion.api.dto.CorreccionManualRequest;
import com.uade.crm.calificacion.api.dto.PreformularioCompletadoEvent;
import com.uade.crm.calificacion.application.facade.CalificacionFacade;
import com.uade.crm.calificacion.domain.event.CalificacionCalculada;
import com.uade.crm.calificacion.domain.model.Calificacion;
import com.uade.crm.calificacion.domain.model.NivelCalificacion;
import com.uade.crm.calificacion.domain.model.OrigenCalificacion;
import com.uade.crm.calificacion.domain.port.CalificacionPersistencePort;
import com.uade.crm.calificacion.domain.port.LeadCalificadoPublisherPort;
import com.uade.crm.calificacion.domain.port.SituacionCrediticiaPort;
import com.uade.crm.calificacion.domain.port.VehiculosPort;
import com.uade.crm.calificacion.domain.reglas.MotorCalificacion;
import com.uade.crm.calificacion.domain.reglas.MotorCalificacionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios de Aplicación - CalificacionFacade")
class CalificacionFacadeTest {

    @Mock
    private VehiculosPort vehiculosPort;

    @Mock
    private SituacionCrediticiaPort situacionCrediticiaPort;

    @Mock
    private CalificacionPersistencePort persistencePort;

    @Mock
    private LeadCalificadoPublisherPort publisherPort;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private CalificacionFacade calificacionFacade;

    @BeforeEach
    void setUp() {
        MotorCalificacion motorCalificacion = MotorCalificacionFactory.crearMotorDefault();
        calificacionFacade = new CalificacionFacade(
                motorCalificacion,
                vehiculosPort,
                situacionCrediticiaPort,
                persistencePort,
                publisherPort,
                eventPublisher
        );
    }

    @Test
    @DisplayName("Debe calificar lead consultando precio vehicular y situacion crediticia por puertos externos")
    void calificarLead_conConsultaPuertosExternos() {
        PreformularioCompletadoEvent event = PreformularioCompletadoEvent.builder()
                .leadId(101L)
                .clienteId(501L)
                .vehiculoId(2001L)
                .montoAnticipo(BigDecimal.valueOf(5_000_000))
                .montoPermuta(BigDecimal.ZERO)
                .solicitaFinanciacion(true)
                .ubicacion("BUENOS_AIRES")
                .build();

        when(vehiculosPort.obtenerPrecioVehiculo(2001L)).thenReturn(Optional.of(BigDecimal.valueOf(10_000_000)));
        when(situacionCrediticiaPort.obtenerSituacionCrediticia(501L)).thenReturn(Optional.of(1));
        when(persistencePort.guardar(any(Calificacion.class))).thenAnswer(invocation -> {
            Calificacion c = invocation.getArgument(0);
            c.setId(99L);
            return c;
        });

        Calificacion resultado = calificacionFacade.calificarLead(event);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(99L);
        assertThat(resultado.getLeadId()).isEqualTo(101L);
        assertThat(resultado.getPuntos()).isEqualTo(80);
        assertThat(resultado.getNivel()).isEqualTo(NivelCalificacion.MUY_POTABLE);
        assertThat(resultado.getOrigen()).isEqualTo(OrigenCalificacion.AUTOMATICA);

        verify(persistencePort, times(1)).guardar(any(Calificacion.class));
        verify(eventPublisher, times(1)).publishEvent(any(CalificacionCalculada.class));
        verify(publisherPort, times(1)).publicar(any(Calificacion.class));
    }

    @Test
    @DisplayName("Debe registrar corrección manual con nivel y motivos personalizados")
    void corregirManualmente_debeGuardarYPublicar() {
        Long leadId = 202L;
        CorreccionManualRequest request = CorreccionManualRequest.builder()
                .nuevoNivel(NivelCalificacion.MUY_POTABLE)
                .puntos(90)
                .motivo("Aprobado por gerencia comercial con fianza")
                .usuario("gerente_ventas")
                .build();

        when(persistencePort.guardar(any(Calificacion.class))).thenAnswer(invocation -> {
            Calificacion c = invocation.getArgument(0);
            c.setId(105L);
            return c;
        });

        Calificacion resultado = calificacionFacade.corregirManualmente(leadId, request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(105L);
        assertThat(resultado.getLeadId()).isEqualTo(202L);
        assertThat(resultado.getNivel()).isEqualTo(NivelCalificacion.MUY_POTABLE);
        assertThat(resultado.getPuntos()).isEqualTo(90);
        assertThat(resultado.getOrigen()).isEqualTo(OrigenCalificacion.MANUAL);

        ArgumentCaptor<Calificacion> captor = ArgumentCaptor.forClass(Calificacion.class);
        verify(persistencePort).guardar(captor.capture());
        Calificacion guardada = captor.getValue();
        assertThat(guardada.getObservacion()).contains("gerente_ventas");
        assertThat(guardada.getMotivos()).hasSize(1);
        assertThat(guardada.getMotivos().getFirst().getRegla()).isEqualTo("CORRECCION_MANUAL");

        verify(eventPublisher, times(1)).publishEvent(any(CalificacionCalculada.class));
        verify(publisherPort, times(1)).publicar(any(Calificacion.class));
    }
}
