package com.uade.crm.calificacion.domain.reglas;

import com.uade.crm.calificacion.domain.model.DatosNegocio;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ReglaCobertura implements ReglaCalificacion {

    public static final String NOMBRE_REGLA = "REGLA_COBERTURA";

    private final int puntosAlta;
    private final int puntosMedia;
    private final int puntosBaja;
    private final double umbralAltaPct;
    private final double umbralMediaPct;

    public ReglaCobertura() {
        this(60, 35, 20, 50.0, 20.0);
    }

    public ReglaCobertura(int puntosAlta, int puntosMedia, int puntosBaja, double umbralAltaPct, double umbralMediaPct) {
        this.puntosAlta = puntosAlta;
        this.puntosMedia = puntosMedia;
        this.puntosBaja = puntosBaja;
        this.umbralAltaPct = umbralAltaPct;
        this.umbralMediaPct = umbralMediaPct;
    }

    @Override
    public ResultadoRegla evaluar(DatosNegocio datos) {
        if (datos == null || datos.getPrecioVehiculo() == null || datos.getPrecioVehiculo().compareTo(BigDecimal.ZERO) <= 0) {
            return ResultadoRegla.builder()
                    .nombreRegla(NOMBRE_REGLA)
                    .puntos(0)
                    .descripcion("Precio de vehículo no disponible o inválido (0 pts)")
                    .aplicada(false)
                    .build();
        }

        BigDecimal anticipo = datos.getMontoAnticipoSeguro();
        BigDecimal permuta = datos.isPermutaNoComputable() ? BigDecimal.ZERO : datos.getMontoPermutaSeguro();
        BigDecimal totalAporte = anticipo.add(permuta);

        if (totalAporte.compareTo(BigDecimal.ZERO) <= 0) {
            return ResultadoRegla.builder()
                    .nombreRegla(NOMBRE_REGLA)
                    .puntos(0)
                    .descripcion(datos.isPermutaNoComputable() && datos.getMontoPermutaSeguro().compareTo(BigDecimal.ZERO) > 0
                            ? "Permuta no computable (clavo) y sin anticipo: 0% cobertura (0 pts)"
                            : "Sin anticipo ni permuta: 0% cobertura (0 pts)")
                    .aplicada(true)
                    .build();
        }

        BigDecimal porcentaje = totalAporte
                .multiply(BigDecimal.valueOf(100))
                .divide(datos.getPrecioVehiculo(), 4, RoundingMode.HALF_UP);

        double pct = porcentaje.doubleValue();
        int puntos;
        String rango;

        if (pct >= umbralAltaPct) {
            puntos = puntosAlta;
            rango = String.format(">= %.0f%% (+%d pts)", umbralAltaPct, puntosAlta);
        } else if (pct >= umbralMediaPct) {
            puntos = puntosMedia;
            rango = String.format("%.0f%% - <%.0f%% (+%d pts)", umbralMediaPct, umbralAltaPct, puntosMedia);
        } else if (pct > 0.0) {
            puntos = puntosBaja;
            rango = String.format("> 0%% - <%.0f%% (+%d pts)", umbralMediaPct, puntosBaja);
        } else {
            puntos = 0;
            rango = "0% (0 pts)";
        }

        String descripcion = String.format("Cobertura aportada: %.2f%% (%s)%s",
                pct,
                rango,
                datos.isPermutaNoComputable() ? " [Permuta descartada por no computable]" : "");

        return ResultadoRegla.builder()
                .nombreRegla(NOMBRE_REGLA)
                .puntos(puntos)
                .descripcion(descripcion)
                .aplicada(true)
                .build();
    }
}
