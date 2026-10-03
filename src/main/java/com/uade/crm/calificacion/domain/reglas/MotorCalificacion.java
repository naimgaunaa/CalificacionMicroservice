package com.uade.crm.calificacion.domain.reglas;

import com.uade.crm.calificacion.domain.model.DatosNegocio;
import com.uade.crm.calificacion.domain.model.NivelCalificacion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MotorCalificacion {

    private final List<ReglaCalificacion> reglas;
    private final int umbralMuyPotable;
    private final int umbralPotable;

    public MotorCalificacion(List<ReglaCalificacion> reglas) {
        this(reglas, 60, 20);
    }

    public MotorCalificacion(List<ReglaCalificacion> reglas, int umbralMuyPotable, int umbralPotable) {
        this.reglas = reglas != null ? new ArrayList<>(reglas) : Collections.emptyList();
        this.umbralMuyPotable = umbralMuyPotable;
        this.umbralPotable = umbralPotable;
    }

    public ResultadoCalificacion calificar(DatosNegocio datos) {
        int puntosTotales = 0;
        List<ResultadoRegla> resultadosReglas = new ArrayList<>();

        for (ReglaCalificacion regla : reglas) {
            ResultadoRegla res = regla.evaluar(datos);
            puntosTotales += res.getPuntos();
            resultadosReglas.add(res);
        }

        NivelCalificacion nivel = determinarNivel(puntosTotales);

        return ResultadoCalificacion.builder()
                .puntosTotales(puntosTotales)
                .nivel(nivel)
                .resultadosReglas(resultadosReglas)
                .build();
    }

    public NivelCalificacion determinarNivel(int puntosTotales) {
        if (puntosTotales >= umbralMuyPotable) {
            return NivelCalificacion.MUY_POTABLE;
        } else if (puntosTotales >= umbralPotable) {
            return NivelCalificacion.POTABLE;
        } else {
            return NivelCalificacion.NO_POTABLE;
        }
    }

    public List<ReglaCalificacion> getReglas() {
        return Collections.unmodifiableList(reglas);
    }
}
