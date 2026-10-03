package com.uade.crm.calificacion.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CalificacionRepository extends JpaRepository<CalificacionEntity, Long> {

    Optional<CalificacionEntity> findTopByLeadIdOrderByFechaCalculoDesc(Long leadId);

    List<CalificacionEntity> findByLeadIdOrderByFechaCalculoDesc(Long leadId);
}
