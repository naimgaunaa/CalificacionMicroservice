package com.uade.crm.calificacion.domain.reglas;

import java.util.List;

public class MotorCalificacionFactory {

    public static MotorCalificacion crearMotorDefault() {
        return new MotorCalificacion(List.of(
                new ReglaCobertura(),
                new ReglaFinanciacion(),
                new ReglaUbicacion()
        ));
    }

    public static MotorCalificacion crearMotor(List<ReglaCalificacion> reglas, int umbralMuyPotable, int umbralPotable) {
        return new MotorCalificacion(reglas, umbralMuyPotable, umbralPotable);
    }
}
