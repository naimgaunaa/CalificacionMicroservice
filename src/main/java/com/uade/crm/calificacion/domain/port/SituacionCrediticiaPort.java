package com.uade.crm.calificacion.domain.port;

import java.util.Optional;

public interface SituacionCrediticiaPort {
    Optional<Integer> obtenerSituacionCrediticia(String clienteId);
}
