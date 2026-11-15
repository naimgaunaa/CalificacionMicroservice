package com.uade.crm.calificacion.domain.reglas;

import com.uade.crm.calificacion.domain.model.DatosNegocio;

/**
 * Penaliza al prospecto que informa un vehículo en parte de pago con deuda de infracciones.
 *
 * <p>Los datos de multas los resuelve el orquestador consultando al scrapper y los entrega a
 * calificacion a través de un comando. Esta regla solo los aplica, si están disponibles:
 * sin información de multas (o sin patente informada) no suma ni resta puntos.
 */
public class ReglaMultas implements ReglaCalificacion {

    public static final String NOMBRE_REGLA = "REGLA_MULTAS";

    private final int puntosConMultas;

    public ReglaMultas() {
        this(-25);
    }

    public ReglaMultas(int puntosConMultas) {
        this.puntosConMultas = puntosConMultas;
    }

    @Override
    public ResultadoRegla evaluar(DatosNegocio datos) {
        if (datos == null || !Boolean.TRUE.equals(datos.getTieneMultas())) {
            return ResultadoRegla.builder()
                    .nombreRegla(NOMBRE_REGLA)
                    .puntos(0)
                    .descripcion("Sin deuda de infracciones verificada para la patente informada (0 pts)")
                    .aplicada(false)
                    .build();
        }

        int cantidad = datos.getCantidadMultas() != null ? datos.getCantidadMultas() : 0;
        return ResultadoRegla.builder()
                .nombreRegla(NOMBRE_REGLA)
                .puntos(puntosConMultas)
                .descripcion(String.format("Patente con %d multa(s) pendiente(s) (%+d pts)", cantidad, puntosConMultas))
                .aplicada(true)
                .build();
    }
}
