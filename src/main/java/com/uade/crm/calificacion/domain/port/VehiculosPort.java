package com.uade.crm.calificacion.domain.port;

import java.math.BigDecimal;
import java.util.Optional;

public interface VehiculosPort {
    Optional<BigDecimal> obtenerPrecioVehiculo(String vehiculoId);
}
