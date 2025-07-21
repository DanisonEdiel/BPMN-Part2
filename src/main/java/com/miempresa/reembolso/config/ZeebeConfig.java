package com.miempresa.reembolso.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para deshabilitar la autoconfiguration de Camunda Zeebe
 * durante el desarrollo, cuando no tenemos un broker Zeebe en ejecución.
 */
@Configuration
@EnableAutoConfiguration(exclude = {
    io.camunda.zeebe.spring.client.CamundaAutoConfiguration.class
})
public class ZeebeConfig {
    // Esta clase vacía solo sirve para deshabilitar la autoconfiguration de Zeebe
}
