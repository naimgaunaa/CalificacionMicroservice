package com.uade.crm.calificacion.domain;

import com.uade.crm.calificacion.domain.model.DatosNegocio;
import com.uade.crm.calificacion.domain.model.NivelCalificacion;
import com.uade.crm.calificacion.domain.reglas.MotorCalificacion;
import com.uade.crm.calificacion.domain.reglas.MotorCalificacionFactory;
import com.uade.crm.calificacion.domain.reglas.ResultadoCalificacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests Unitarios de Dominio - MotorCalificacion")
class MotorCalificacionTest {

    private static final BigDecimal PRECIO_UNIDAD_10M = BigDecimal.valueOf(10_000_000);
    private MotorCalificacion motorCalificacion;

    @BeforeEach
    void setUp() {
        motorCalificacion = MotorCalificacionFactory.crearMotorDefault();
    }

    @Test
    @DisplayName("Caso 1: Anticipo 3M, permuta 3M, BA, sin financiación -> 60 pts (MUY_POTABLE)")
    void caso1_anticipo3M_permuta3M_ba_sinFinanciacion_debeSerMuyPotable() {
        DatosNegocio datos = DatosNegocio.builder()
                .leadId(1L)
                .precioVehiculo(PRECIO_UNIDAD_10M)
                .montoAnticipo(BigDecimal.valueOf(3_000_000))
                .montoPermuta(BigDecimal.valueOf(3_000_000))
                .permutaNoComputable(false)
                .ubicacion("BUENOS_AIRES")
                .solicitaFinanciacion(false)
                .build();

        ResultadoCalificacion resultado = motorCalificacion.calificar(datos);

        assertThat(resultado.getPuntosTotales()).isEqualTo(60);
        assertThat(resultado.getNivel()).isEqualTo(NivelCalificacion.MUY_POTABLE);
    }

    @Test
    @DisplayName("Caso 2: Anticipo 3M, permuta 3M, Córdoba, sin financiación -> 35 pts (POTABLE)")
    void caso2_anticipo3M_permuta3M_cordoba_sinFinanciacion_debeSerPotable() {
        DatosNegocio datos = DatosNegocio.builder()
                .leadId(2L)
                .precioVehiculo(PRECIO_UNIDAD_10M)
                .montoAnticipo(BigDecimal.valueOf(3_000_000))
                .montoPermuta(BigDecimal.valueOf(3_000_000))
                .permutaNoComputable(false)
                .ubicacion("Córdoba")
                .solicitaFinanciacion(false)
                .build();

        ResultadoCalificacion resultado = motorCalificacion.calificar(datos);

        // 60 pts cobertura (60%) - 25 pts fuera de zona = 35 pts
        assertThat(resultado.getPuntosTotales()).isEqualTo(35);
        assertThat(resultado.getNivel()).isEqualTo(NivelCalificacion.POTABLE);
    }

    @Test
    @DisplayName("Caso 3: Anticipo 3M, permuta 0, BA, financiación sit 1 -> 55 pts (POTABLE)")
    void caso3_anticipo3M_sinPermuta_ba_financiacionSit1_debeSerPotable() {
        DatosNegocio datos = DatosNegocio.builder()
                .leadId(3L)
                .precioVehiculo(PRECIO_UNIDAD_10M)
                .montoAnticipo(BigDecimal.valueOf(3_000_000))
                .montoPermuta(BigDecimal.ZERO)
                .permutaNoComputable(false)
                .ubicacion("BUENOS_AIRES")
                .solicitaFinanciacion(true)
                .situacionCrediticia(1)
                .build();

        ResultadoCalificacion resultado = motorCalificacion.calificar(datos);

        // 35 pts cobertura (30%) + 20 pts sit 1 + 0 pts BA = 55 pts
        assertThat(resultado.getPuntosTotales()).isEqualTo(55);
        assertThat(resultado.getNivel()).isEqualTo(NivelCalificacion.POTABLE);
    }

    @Test
    @DisplayName("Caso 4: Anticipo 5M, permuta 0, BA, financiación sit 1 -> 80 pts (MUY_POTABLE)")
    void caso4_anticipo5M_sinPermuta_ba_financiacionSit1_debeSerMuyPotable() {
        DatosNegocio datos = DatosNegocio.builder()
                .leadId(4L)
                .precioVehiculo(PRECIO_UNIDAD_10M)
                .montoAnticipo(BigDecimal.valueOf(5_000_000))
                .montoPermuta(BigDecimal.ZERO)
                .permutaNoComputable(false)
                .ubicacion("BUENOS_AIRES")
                .solicitaFinanciacion(true)
                .situacionCrediticia(1)
                .build();

        ResultadoCalificacion resultado = motorCalificacion.calificar(datos);

        // 60 pts cobertura (50%) + 20 pts sit 1 = 80 pts
        assertThat(resultado.getPuntosTotales()).isEqualTo(80);
        assertThat(resultado.getNivel()).isEqualTo(NivelCalificacion.MUY_POTABLE);
    }

    @Test
    @DisplayName("Caso 5: Sin anticipo ni permuta, financiación sit 1 -> 20 pts (POTABLE)")
    void caso5_sinAporte_ba_financiacionSit1_debeSerPotable() {
        DatosNegocio datos = DatosNegocio.builder()
                .leadId(5L)
                .precioVehiculo(PRECIO_UNIDAD_10M)
                .montoAnticipo(BigDecimal.ZERO)
                .montoPermuta(BigDecimal.ZERO)
                .permutaNoComputable(false)
                .ubicacion("BUENOS_AIRES")
                .solicitaFinanciacion(true)
                .situacionCrediticia(1)
                .build();

        ResultadoCalificacion resultado = motorCalificacion.calificar(datos);

        // 0 pts cobertura + 20 pts sit 1 = 20 pts
        assertThat(resultado.getPuntosTotales()).isEqualTo(20);
        assertThat(resultado.getNivel()).isEqualTo(NivelCalificacion.POTABLE);
    }

    @Test
    @DisplayName("Caso 6: Sin anticipo, sin permuta, sin financiación -> 0 pts (NO_POTABLE)")
    void caso6_sinAporte_sinFinanciacion_debeSerNoPotable() {
        DatosNegocio datos = DatosNegocio.builder()
                .leadId(6L)
                .precioVehiculo(PRECIO_UNIDAD_10M)
                .montoAnticipo(BigDecimal.ZERO)
                .montoPermuta(BigDecimal.ZERO)
                .permutaNoComputable(false)
                .ubicacion("BUENOS_AIRES")
                .solicitaFinanciacion(false)
                .build();

        ResultadoCalificacion resultado = motorCalificacion.calificar(datos);

        assertThat(resultado.getPuntosTotales()).isEqualTo(0);
        assertThat(resultado.getNivel()).isEqualTo(NivelCalificacion.NO_POTABLE);
    }

    @Test
    @DisplayName("Caso 7: Sin anticipo ni permuta, financiación sit 4 -> -25 pts (NO_POTABLE)")
    void caso7_sinAporte_financiacionSit4_debeSerNoPotable() {
        DatosNegocio datos = DatosNegocio.builder()
                .leadId(7L)
                .precioVehiculo(PRECIO_UNIDAD_10M)
                .montoAnticipo(BigDecimal.ZERO)
                .montoPermuta(BigDecimal.ZERO)
                .permutaNoComputable(false)
                .ubicacion("BUENOS_AIRES")
                .solicitaFinanciacion(true)
                .situacionCrediticia(4)
                .build();

        ResultadoCalificacion resultado = motorCalificacion.calificar(datos);

        // 0 pts cobertura - 25 pts sit 4 = -25 pts
        assertThat(resultado.getPuntosTotales()).isEqualTo(-25);
        assertThat(resultado.getNivel()).isEqualTo(NivelCalificacion.NO_POTABLE);
    }

    @Test
    @DisplayName("Caso 8: Anticipo 1M, sin permuta, sin financiación -> 20 pts (POTABLE)")
    void caso8_anticipo1M_sinPermuta_sinFinanciacion_debeSerPotable() {
        DatosNegocio datos = DatosNegocio.builder()
                .leadId(8L)
                .precioVehiculo(PRECIO_UNIDAD_10M)
                .montoAnticipo(BigDecimal.valueOf(1_000_000))
                .montoPermuta(BigDecimal.ZERO)
                .permutaNoComputable(false)
                .ubicacion("BUENOS_AIRES")
                .solicitaFinanciacion(false)
                .build();

        ResultadoCalificacion resultado = motorCalificacion.calificar(datos);

        // 20 pts cobertura (10%) + 0 pts = 20 pts
        assertThat(resultado.getPuntosTotales()).isEqualTo(20);
        assertThat(resultado.getNivel()).isEqualTo(NivelCalificacion.POTABLE);
    }

    @Test
    @DisplayName("Caso 9: Permuta 6M marcada como clavo (no computable), sin anticipo ni financiación -> 0 pts (NO_POTABLE)")
    void caso9_permuta6MClavo_sinAnticipo_sinFinanciacion_debeSerNoPotable() {
        DatosNegocio datos = DatosNegocio.builder()
                .leadId(9L)
                .precioVehiculo(PRECIO_UNIDAD_10M)
                .montoAnticipo(BigDecimal.ZERO)
                .montoPermuta(BigDecimal.valueOf(6_000_000))
                .permutaNoComputable(true) // Vehículo clavo / no computable
                .ubicacion("BUENOS_AIRES")
                .solicitaFinanciacion(false)
                .build();

        ResultadoCalificacion resultado = motorCalificacion.calificar(datos);

        // Permuta clavo computa 0 -> 0% cobertura -> 0 pts
        assertThat(resultado.getPuntosTotales()).isEqualTo(0);
        assertThat(resultado.getNivel()).isEqualTo(NivelCalificacion.NO_POTABLE);
    }
}
