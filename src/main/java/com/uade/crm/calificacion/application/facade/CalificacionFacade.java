package com.uade.crm.calificacion.application.facade;

import com.uade.crm.calificacion.api.dto.CorreccionManualRequest;
import com.uade.crm.calificacion.api.dto.PreformularioCompletadoEvent;
import com.uade.crm.calificacion.domain.event.CalificacionCalculada;
import com.uade.crm.calificacion.domain.model.*;
import com.uade.crm.calificacion.domain.port.CalificacionPersistencePort;
import com.uade.crm.calificacion.domain.port.LeadCalificadoPublisherPort;
import com.uade.crm.calificacion.domain.port.SituacionCrediticiaPort;
import com.uade.crm.calificacion.domain.port.VehiculosPort;
import com.uade.crm.calificacion.domain.reglas.MotorCalificacion;
import com.uade.crm.calificacion.domain.reglas.ResultadoCalificacion;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalificacionFacade {

    private final MotorCalificacion motorCalificacion;
    private final VehiculosPort vehiculosPort;
    private final SituacionCrediticiaPort situacionCrediticiaPort;
    private final CalificacionPersistencePort persistencePort;
    private final LeadCalificadoPublisherPort publisherPort;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Calificacion calificarLead(PreformularioCompletadoEvent event) {
        if (event == null || event.getLeadId() == null) {
            throw new IllegalArgumentException("El evento de preformulario y el leadId son obligatorios");
        }

        log.info("Iniciando proceso de calificación para leadId: {}", event.getLeadId());

        // 1. Resolver precio de la unidad
        BigDecimal precioVehiculo = event.getPrecioVehiculo();
        if (precioVehiculo == null && event.getVehiculoId() != null) {
            precioVehiculo = vehiculosPort.obtenerPrecioVehiculo(event.getVehiculoId()).orElse(null);
            log.info("Precio de vehículo {} obtenido por puerto: {}", event.getVehiculoId(), precioVehiculo);
        }

        // 2. Resolver situación crediticia si solicita financiación
        Integer situacionCrediticia = event.getSituacionCrediticia();
        if (event.isSolicitaFinanciacion() && situacionCrediticia == null && event.getClienteId() != null) {
            situacionCrediticia = situacionCrediticiaPort.obtenerSituacionCrediticia(event.getClienteId()).orElse(null);
            log.info("Situación crediticia de cliente {} obtenida por puerto: {}", event.getClienteId(), situacionCrediticia);
        }

        // 3. Armar datos de negocio para el motor
        DatosNegocio datos = DatosNegocio.builder()
                .leadId(event.getLeadId())
                .vehiculoId(event.getVehiculoId())
                .precioVehiculo(precioVehiculo)
                .montoAnticipo(event.getMontoAnticipo())
                .montoPermuta(event.getMontoPermuta())
                .permutaNoComputable(event.isPermutaNoComputable())
                .solicitaFinanciacion(event.isSolicitaFinanciacion())
                .situacionCrediticia(situacionCrediticia)
                .ubicacion(event.getUbicacion())
                .build();

        // 4. Evaluar con el motor de reglas
        ResultadoCalificacion resultado = motorCalificacion.calificar(datos);

        // 5. Construir entidad de dominio Calificacion
        List<MotivoCalificacion> motivos = resultado.getResultadosReglas().stream()
                .map(r -> MotivoCalificacion.builder()
                        .regla(r.getNombreRegla())
                        .puntos(r.getPuntos())
                        .descripcion(r.getDescripcion())
                        .build())
                .toList();

        Calificacion calificacion = Calificacion.builder()
                .leadId(event.getLeadId())
                .puntos(resultado.getPuntosTotales())
                .nivel(resultado.getNivel())
                .origen(OrigenCalificacion.AUTOMATICA)
                .fechaCalculo(LocalDateTime.now())
                .observacion("Calificación automática calculada por motor de reglas")
                .motivos(new ArrayList<>(motivos))
                .build();

        // 6. Persistir en base de datos
        Calificacion guardada = persistencePort.guardar(calificacion);
        log.info("Calificación persistida con ID: {} para leadId: {} (Nivel: {}, Puntos: {})",
                guardada.getId(), guardada.getLeadId(), guardada.getNivel(), guardada.getPuntos());

        // 7. Notificar evento interno de dominio
        eventPublisher.publishEvent(new CalificacionCalculada(guardada));

        // 8. Publicar evento de integración en Kafka
        publisherPort.publicar(guardada);

        return guardada;
    }

    @Transactional
    public Calificacion corregirManualmente(Long leadId, CorreccionManualRequest request) {
        if (leadId == null) {
            throw new IllegalArgumentException("El leadId es obligatorio para una corrección manual");
        }

        log.info("Realizando corrección manual para leadId: {} a nuevo nivel: {}", leadId, request.getNuevoNivel());

        // Determinar puntos: si se proveen en el request se usan, sino se asignan puntos acordes al nuevo nivel
        int puntos;
        if (request.getPuntos() != null) {
            puntos = request.getPuntos();
        } else {
            puntos = switch (request.getNuevoNivel()) {
                case MUY_POTABLE -> 60;
                case POTABLE -> 35;
                case NO_POTABLE -> 0;
            };
        }

        String autor = request.getUsuario() != null && !request.getUsuario().isBlank()
                ? request.getUsuario()
                : "Asesor Comercial";

        MotivoCalificacion motivoCorreccion = MotivoCalificacion.builder()
                .regla("CORRECCION_MANUAL")
                .puntos(puntos)
                .descripcion(String.format("Corrección manual por %s: %s", autor, request.getMotivo()))
                .build();

        Calificacion calificacionManual = Calificacion.builder()
                .leadId(leadId)
                .puntos(puntos)
                .nivel(request.getNuevoNivel())
                .origen(OrigenCalificacion.MANUAL)
                .fechaCalculo(LocalDateTime.now())
                .observacion(String.format("Corrección manual realizada por %s. Motivo: %s", autor, request.getMotivo()))
                .motivos(new ArrayList<>(List.of(motivoCorreccion)))
                .build();

        Calificacion guardada = persistencePort.guardar(calificacionManual);

        // Notificar interna y externamente
        eventPublisher.publishEvent(new CalificacionCalculada(guardada));
        publisherPort.publicar(guardada);

        return guardada;
    }

    @Transactional(readOnly = true)
    public Calificacion obtenerUltimaPorLeadId(Long leadId) {
        return persistencePort.buscarUltimaPorLeadId(leadId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró ninguna calificación para el lead " + leadId));
    }

    @Transactional(readOnly = true)
    public List<Calificacion> obtenerHistorialPorLeadId(Long leadId) {
        return persistencePort.buscarHistorialPorLeadId(leadId);
    }
}
