package com.uade.crm.calificacion.infrastructure.persistence;

import com.uade.crm.calificacion.domain.model.Calificacion;
import com.uade.crm.calificacion.domain.model.MotivoCalificacion;
import com.uade.crm.calificacion.domain.port.CalificacionPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CalificacionPersistenceAdapter implements CalificacionPersistencePort {

    private final CalificacionRepository repository;

    @Override
    @Transactional
    public Calificacion guardar(Calificacion calificacion) {
        CalificacionEntity entity = toEntity(calificacion);
        CalificacionEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Calificacion> buscarUltimaPorLeadId(Long leadId) {
        return repository.findTopByLeadIdOrderByFechaCalculoDesc(leadId)
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Calificacion> buscarHistorialPorLeadId(Long leadId) {
        return repository.findByLeadIdOrderByFechaCalculoDesc(leadId).stream()
                .map(this::toDomain)
                .toList();
    }

    private CalificacionEntity toEntity(Calificacion domain) {
        if (domain == null) return null;

        List<MotivoCalificacionEntity> motivosEntities = new ArrayList<>();
        if (domain.getMotivos() != null) {
            for (MotivoCalificacion m : domain.getMotivos()) {
                motivosEntities.add(MotivoCalificacionEntity.builder()
                        .id(m.getId())
                        .regla(m.getRegla())
                        .puntos(m.getPuntos())
                        .descripcion(m.getDescripcion())
                        .build());
            }
        }

        return CalificacionEntity.builder()
                .id(domain.getId())
                .leadId(domain.getLeadId())
                .puntos(domain.getPuntos())
                .nivel(domain.getNivel())
                .origen(domain.getOrigen())
                .fechaCalculo(domain.getFechaCalculo())
                .observacion(domain.getObservacion())
                .motivos(motivosEntities)
                .build();
    }

    private Calificacion toDomain(CalificacionEntity entity) {
        if (entity == null) return null;

        List<MotivoCalificacion> motivos = new ArrayList<>();
        if (entity.getMotivos() != null) {
            for (MotivoCalificacionEntity m : entity.getMotivos()) {
                motivos.add(MotivoCalificacion.builder()
                        .id(m.getId())
                        .regla(m.getRegla())
                        .puntos(m.getPuntos())
                        .descripcion(m.getDescripcion())
                        .build());
            }
        }

        return Calificacion.builder()
                .id(entity.getId())
                .leadId(entity.getLeadId())
                .puntos(entity.getPuntos())
                .nivel(entity.getNivel())
                .origen(entity.getOrigen())
                .fechaCalculo(entity.getFechaCalculo())
                .observacion(entity.getObservacion())
                .motivos(motivos)
                .build();
    }
}
