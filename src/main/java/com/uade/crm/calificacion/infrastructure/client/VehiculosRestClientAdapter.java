package com.uade.crm.calificacion.infrastructure.client;

import com.uade.crm.calificacion.domain.port.VehiculosPort;
import com.uade.crm.calificacion.infrastructure.client.dto.VehiculoClientResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Component
public class VehiculosRestClientAdapter implements VehiculosPort {

    private final RestClient vehiculosRestClient;

    public VehiculosRestClientAdapter(@Qualifier("vehiculosRestClient") RestClient vehiculosRestClient) {
        this.vehiculosRestClient = vehiculosRestClient;
    }

    @Override
    public Optional<BigDecimal> obtenerPrecioVehiculo(Long vehiculoId) {
        if (vehiculoId == null) {
            return Optional.empty();
        }

        try {
            VehiculoClientResponse response = vehiculosRestClient.get()
                    .uri("/vehiculos/{id}", vehiculoId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        log.warn("Vehículo con ID {} no encontrado en microservicio de vehículos (Status: {})", vehiculoId, res.getStatusCode());
                    })
                    .body(VehiculoClientResponse.class);

            if (response != null && response.getPrecioEfectivo() != null) {
                return Optional.of(response.getPrecioEfectivo());
            }
        } catch (Exception ex) {
            log.error("Error al consultar precio del vehículo {} en el microservicio de vehículos: {}", vehiculoId, ex.getMessage());
        }

        return Optional.empty();
    }
}
