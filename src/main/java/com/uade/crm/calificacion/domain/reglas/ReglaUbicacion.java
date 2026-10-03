package com.uade.crm.calificacion.domain.reglas;

import com.uade.crm.calificacion.domain.model.DatosNegocio;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReglaUbicacion implements ReglaCalificacion {

    public static final String NOMBRE_REGLA = "REGLA_UBICACION";

    private final int penalizacionFueraZona;
    private final Set<String> zonasPermitidasNormalizadas;

    public ReglaUbicacion() {
        this(-25, List.of("BUENOS_AIRES", "BUENOS AIRES", "CABA", "BSAS", "BS_AS", "GBA", "BA"));
    }

    public ReglaUbicacion(int penalizacionFueraZona, List<String> zonasPermitidas) {
        this.penalizacionFueraZona = penalizacionFueraZona;
        this.zonasPermitidasNormalizadas = new HashSet<>();
        if (zonasPermitidas != null) {
            for (String zona : zonasPermitidas) {
                this.zonasPermitidasNormalizadas.add(normalizar(zona));
            }
        }
    }

    @Override
    public ResultadoRegla evaluar(DatosNegocio datos) {
        if (datos == null || datos.getUbicacion() == null || datos.getUbicacion().isBlank()) {
            return ResultadoRegla.builder()
                    .nombreRegla(NOMBRE_REGLA)
                    .puntos(0)
                    .descripcion("Ubicación no especificada (se asume zona estándar, 0 pts)")
                    .aplicada(false)
                    .build();
        }

        String ubicacionNorm = normalizar(datos.getUbicacion());
        boolean enZona = false;

        for (String zonaPermitida : zonasPermitidasNormalizadas) {
            if (ubicacionNorm.equals(zonaPermitida) || (zonaPermitida.length() >= 4 && ubicacionNorm.contains(zonaPermitida))) {
                enZona = true;
                break;
            }
        }

        if (enZona) {
            return ResultadoRegla.builder()
                    .nombreRegla(NOMBRE_REGLA)
                    .puntos(0)
                    .descripcion(String.format("Ubicación en zona preferencial (%s): 0 pts", datos.getUbicacion()))
                    .aplicada(true)
                    .build();
        }

        return ResultadoRegla.builder()
                .nombreRegla(NOMBRE_REGLA)
                .puntos(penalizacionFueraZona)
                .descripcion(String.format("Ubicación fuera de zona preferencial (%s): %d pts", datos.getUbicacion(), penalizacionFueraZona))
                .aplicada(true)
                .build();
    }

    private String normalizar(String texto) {
        if (texto == null) return "";
        String normalized = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase()
                .trim()
                .replaceAll("[^A-Z0-9]", "");
        return normalized;
    }
}
