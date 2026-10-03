package com.uade.crm.calificacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CalificacionApplication {

    public static void main(String[] args) {
        SpringApplication.run(CalificacionApplication.class, args);
    }
}
