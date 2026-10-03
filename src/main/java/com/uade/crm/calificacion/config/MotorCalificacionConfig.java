package com.uade.crm.calificacion.config;

import com.uade.crm.calificacion.domain.reglas.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class MotorCalificacionConfig {

    @Bean
    public MotorCalificacion motorCalificacion(ReglasCalificacionProperties properties) {
        ReglaCobertura reglaCobertura = new ReglaCobertura(
                properties.getPuntos().getCoberturaAlta(),
                properties.getPuntos().getCoberturaMedia(),
                properties.getPuntos().getCoberturaBaja(),
                properties.getUmbrales().getCoberturaAltaPct(),
                properties.getUmbrales().getCoberturaMediaPct()
        );

        ReglaFinanciacion reglaFinanciacion = new ReglaFinanciacion(
                properties.getPuntos().getFinanciacionBuena(),
                properties.getPuntos().getFinanciacionMala()
        );

        ReglaUbicacion reglaUbicacion = new ReglaUbicacion(
                properties.getPuntos().getUbicacionFueraZona(),
                properties.getZonasAdmitidas()
        );

        return MotorCalificacionFactory.crearMotor(
                List.of(reglaCobertura, reglaFinanciacion, reglaUbicacion),
                properties.getUmbrales().getNivelMuyPotable(),
                properties.getUmbrales().getNivelPotable()
        );
    }
}
