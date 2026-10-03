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

    @Bean
    public RestClient clientesRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));

        return RestClient.builder()
                .baseUrl(clientesServiceUrl)
                .requestFactory(factory)
                .build();
    }

    @Bean
    public RestClient vehiculosRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));

        return RestClient.builder()
                .baseUrl(vehiculosServiceUrl)
                .requestFactory(factory)
                .build();
    }
}
