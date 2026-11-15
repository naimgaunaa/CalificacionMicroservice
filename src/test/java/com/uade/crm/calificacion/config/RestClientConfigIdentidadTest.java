package com.uade.crm.calificacion.config;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * calificacion llama a clientes y vehiculos a traves del orquestador (API Gateway). Para que este lo deje pasar sin
 * login tiene que identificarse con X-Origen-Servicio y X-Clave-Servicio.
 */
class RestClientConfigIdentidadTest {

    private HttpServer servidor;
    private final AtomicReference<com.sun.net.httpserver.Headers> recibidos = new AtomicReference<>();

    @BeforeEach
    void levantarServidor() throws Exception {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", exchange -> {
            recibidos.set(exchange.getRequestHeaders());
            byte[] cuerpo = "{}".getBytes();
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, cuerpo.length);
            exchange.getResponseBody().write(cuerpo);
            exchange.close();
        });
        servidor.start();
    }

    @AfterEach
    void apagarServidor() {
        servidor.stop(0);
    }

    private String url() {
        return "http://127.0.0.1:" + servidor.getAddress().getPort();
    }

    private RestClientConfig config(String clave) {
        RestClientConfig config = new RestClientConfig();
        ReflectionTestUtils.setField(config, "clientesServiceUrl", url());
        ReflectionTestUtils.setField(config, "vehiculosServiceUrl", url());
        ReflectionTestUtils.setField(config, "claveServicioOrquestador", clave);
        return config;
    }

    private void llamar(RestClient cliente) {
        cliente.get().uri("/algo").retrieve().toBodilessEntity();
    }

    @Test
    void conClaveSeIdentificaComoCalificacionAlLlamarAClientes() {
        llamar(config("c".repeat(32)).clientesRestClient());

        assertThat(recibidos.get().getFirst("X-Origen-Servicio")).isEqualTo("calificacion");
        assertThat(recibidos.get().getFirst("X-Clave-Servicio")).isEqualTo("c".repeat(32));
    }

    @Test
    void conClaveSeIdentificaComoCalificacionAlLlamarAVehiculos() {
        llamar(config("c".repeat(32)).vehiculosRestClient());

        assertThat(recibidos.get().getFirst("X-Origen-Servicio")).isEqualTo("calificacion");
        assertThat(recibidos.get().getFirst("X-Clave-Servicio")).isEqualTo("c".repeat(32));
    }

    @Test
    void sinClaveNoMandaNingunHeaderDeIdentidad() {
        llamar(config("").clientesRestClient());

        assertThat(recibidos.get().containsKey("X-Origen-Servicio")).isFalse();
        assertThat(recibidos.get().containsKey("X-Clave-Servicio")).isFalse();
    }

    @Test
    void unaClaveEnBlancoCuentaComoAusente() {
        llamar(config("   ").vehiculosRestClient());

        assertThat(recibidos.get().containsKey("X-Clave-Servicio")).isFalse();
    }
}
