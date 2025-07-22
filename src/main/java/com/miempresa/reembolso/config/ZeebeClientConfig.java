package com.miempresa.reembolso.config;

import io.camunda.zeebe.client.ZeebeClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración del cliente Zeebe para interactuar con el broker
 */
@Configuration
public class ZeebeClientConfig {

    @Value("${zeebe.client.broker.gateway-address}")
    private String gatewayAddress;

    @Value("${zeebe.client.security.plaintext}")
    private boolean plaintext;

    /**
     * Crea y configura el cliente Zeebe como un bean de Spring
     */
    @Bean
    public ZeebeClient zeebeClient() {
        ZeebeClient client;
        
        if (plaintext) {
            client = ZeebeClient.newClientBuilder()
                    .gatewayAddress(gatewayAddress)
                    .usePlaintext()
                    .build();
        } else {
            client = ZeebeClient.newClientBuilder()
                    .gatewayAddress(gatewayAddress)
                    .build();
        }
        
        return client;
    }
}
