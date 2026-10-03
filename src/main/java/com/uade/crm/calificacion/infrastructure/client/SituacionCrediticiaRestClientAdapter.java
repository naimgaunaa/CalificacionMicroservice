package com.uade.crm.calificacion.infrastructure.client;

import com.uade.crm.calificacion.domain.port.SituacionCrediticiaPort;
import com.uade.crm.calificacion.infrastructure.client.dto.SituacionCrediticiaClientResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Slf4j
@Component
public class SituacionCrediticiaRestClientAdapter implements SituacionCrediticiaPort {

    private final RestClient clientesRestClient;

    public SituacionCrediticiaRestClientAdapter(@Qualifier("clientesRestClient") RestClient clientesRestClient) {
        this.clientesRestClient = clientesRestClient;
    }

    @Override
    public Optional<Integer> obtenerSituacionCrediticia(Long clienteId) {
        if (clienteId == null) {
            return Optional.empty();
        }

        try {
            SituacionCrediticiaClientResponse response = clientesRestClient.get()
                    .uri("/clientes/{id}/situacion-crediticia", clienteId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        log.warn("Situación crediticia para cliente {} no encontrada en microservicio de clientes (Status: {})", clienteId, res.getStatusCode());
                    })
                    .body(SituacionCrediticiaClientResponse.class);

            if (response != null && response.getSituacionEfectiva() != null) {
                return Optional.of(response.getSituacionEfectiva());
            }
        } catch (Exception ex) {
            log.error("Error al consultar situación crediticia del cliente {} en el microservicio de clientes: {}", clienteId, ex.getMessage());
        }

        return Optional.empty();
    }
}
