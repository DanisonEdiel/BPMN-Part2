package com.miempresa.reembolso.config;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * Despliega los formularios programáticamente al iniciar la aplicación
 */
@Component
public class FormDeployer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(FormDeployer.class);

    @Autowired
    private ZeebeClient zeebeClient;

    @Override
    public void run(String... args) throws Exception {
        deployForms();
    }

    /**
     * Despliega los formularios directamente usando el ZeebeClient
     */
    private void deployForms() {
        try {
            // Leer los archivos de formulario
            String solicitudReembolsoForm = readResourceAsString("classpath:forms/solicitudReembolso.form");
            String revisionSupervisorForm = readResourceAsString("classpath:forms/revisionSupervisor.form");

            // Desplegar los formularios
            DeploymentEvent deploymentEvent = zeebeClient.newDeployResourceCommand()
                    .addResourceStringUtf8(solicitudReembolsoForm, "solicitudReembolso.form")
                    .addResourceStringUtf8(revisionSupervisorForm, "revisionSupervisor.form")
                    .send()
                    .join();

            logger.info("Formularios desplegados correctamente: {}", deploymentEvent.getKey());
            deploymentEvent.getDeployedResources().forEach(resource -> 
                logger.info("Recurso desplegado: {} ({})", resource.getResourceName(), resource.getResourceType()));

        } catch (Exception e) {
            logger.error("Error al desplegar formularios: {}", e.getMessage(), e);
        }
    }

    /**
     * Lee un recurso como string
     */
    private String readResourceAsString(String resourcePath) throws IOException {
        Resource resource = new ClassPathResource(resourcePath.replace("classpath:", ""));
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
    }
}
