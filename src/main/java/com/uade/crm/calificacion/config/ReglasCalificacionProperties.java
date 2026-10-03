package com.uade.crm.calificacion.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "crm.reglas")
public class ReglasCalificacionProperties {

    private Puntos puntos = new Puntos();
    private Umbrales umbrales = new Umbrales();
    private List<String> zonasAdmitidas = new ArrayList<>(List.of(
            "BUENOS_AIRES", "BUENOS AIRES", "CABA", "BSAS", "BS_AS", "GBA", "BA"
    ));

    @Data
    public static class Puntos {
        private int coberturaAlta = 60;
        private int coberturaMedia = 35;
        private int coberturaBaja = 20;
        private int financiacionBuena = 20;
        private int financiacionMala = -25;
        private int ubicacionFueraZona = -25;
    }

    @Data
    public static class Umbrales {
        private double coberturaAltaPct = 50.0;
        private double coberturaMediaPct = 20.0;
        private int nivelMuyPotable = 60;
        private int nivelPotable = 20;
    }
}
