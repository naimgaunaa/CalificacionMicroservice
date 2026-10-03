package com.uade.crm.calificacion.domain.reglas;

import com.uade.crm.calificacion.domain.model.DatosNegocio;

public class ReglaFinanciacion implements ReglaCalificacion {

    public static final String NOMBRE_REGLA = "REGLA_FINANCIACION";

    private final int puntosFavorable;
    private final int puntosDesfavorable;

    public ReglaFinanciacion() {
        this(20, -25);
    }

    public ReglaFinanciacion(int puntosFavorable, int puntosDesfavorable) {
        this.puntosFavorable = puntosFavorable;
        this.puntosDesfavorable = puntosDesfavorable;
    }

    @Override
    public ResultadoRegla evaluar(DatosNegocio datos) {
        if (datos == null || !datos.isSolicitaFinanciacion()) {
            return ResultadoRegla.builder()
                    .nombreRegla(NOMBRE_REGLA)
                    .puntos(0)
                    .descripcion("No solicita financiación (0 pts)")
                    .aplicada(false)
                    .build();
        }

        Integer situacion = datos.getSituacionCrediticia();
        if (situacion == null || situacion <= 0) {
            return ResultadoRegla.builder()
                    .nombreRegla(NOMBRE_REGLA)
                    .puntos(0)
                    .descripcion("Solicita financiación pero no cuenta con datos de situación crediticia BCRA (0 pts)")
                    .aplicada(true)
                    .build();
        }

        if (situacion == 1 || situacion == 2) {
            return ResultadoRegla.builder()
                    .nombreRegla(NOMBRE_REGLA)
                    .puntos(puntosFavorable)
                    .descripcion(String.format("Financiación con situación crediticia favorable (Sit %d): +%d pts", situacion, puntosFavorable))
                    .aplicada(true)
                    .build();
        }

        if (situacion >= 3 && situacion <= 6) {
            return ResultadoRegla.builder()
                    .nombreRegla(NOMBRE_REGLA)
                    .puntos(puntosDesfavorable)
                    .descripcion(String.format("Financiación con situación crediticia de riesgo (Sit %d): %d pts", situacion, puntosDesfavorable))
                    .aplicada(true)
                    .build();
        }

        return ResultadoRegla.builder()
                .nombreRegla(NOMBRE_REGLA)
                .puntos(0)
                .descripcion(String.format("Situación crediticia fuera de rango BCRA (%d): 0 pts", situacion))
                .aplicada(true)
                .build();
    }
}
