package com.uade.crm.calificacion.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Value("${crm.servicios.clientes.url:http://localhost:8082}")
    private String clientesServiceUrl;

    @Value("${crm.servicios.vehiculos.url:http://localhost:8083}")
    private String vehiculosServiceUrl;

    /**
     * Credencial de calificacion ante el orquestador (API Gateway). Obligatoria si las URLs de clientes y vehiculos
     * apuntan al orquestador (<orquestador>/api); vacia si se llama directo a los servicios.
     */
    @Value("${crm.orquestador.clave-servicio:}")
    private String claveServicioOrquestador;

    @Bean
    public RestClient clientesRestClient() {
        return construir(clientesServiceUrl);
    }

    @Bean
    public RestClient vehiculosRestClient() {
        return construir(vehiculosServiceUrl);
    }

    private RestClient construir(String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory);

        if (claveServicioOrquestador != null && !claveServicioOrquestador.isBlank()) {
            // Asi el orquestador sabe que llama calificacion (y no cualquiera) y lo deja pasar sin login.
            builder.defaultHeader("X-Origen-Servicio", "calificacion");
            builder.defaultHeader("X-Clave-Servicio", claveServicioOrquestador.trim());
        }
        return builder.build();
    }
}
