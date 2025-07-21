package com.miempresa.reembolso.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Configuration class for deploying form files to the classpath.
 * This ensures forms are available at runtime for Camunda 8 to find.
 */
@Configuration
public class FormDeploymentConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(FormDeploymentConfig.class);

    @Bean
    public String deployFormsToClasspath() {
        try {
            // Create directory if it doesn't exist
            Path formsDir = Paths.get(System.getProperty("user.dir"), "target", "classes", "forms");
            if (!Files.exists(formsDir)) {
                Files.createDirectories(formsDir);
                LOGGER.info("Created forms directory: {}", formsDir);
            }

            // Copy form files to target directory
            Resource solicitudForm = new ClassPathResource("/forms/solicitudReembolso.form");
            Resource revisionForm = new ClassPathResource("/forms/revisionSupervisor.form");
            
            if (solicitudForm.exists()) {
                Path targetPath = Paths.get(formsDir.toString(), "solicitudReembolso.form");
                Files.copy(solicitudForm.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                LOGGER.info("Form deployed to classpath: solicitudReembolso.form to {}", targetPath);
            } else {
                LOGGER.error("Form not found: solicitudReembolso.form");
            }
            
            if (revisionForm.exists()) {
                Path targetPath = Paths.get(formsDir.toString(), "revisionSupervisor.form");
                Files.copy(revisionForm.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                LOGGER.info("Form deployed to classpath: revisionSupervisor.form to {}", targetPath);
            } else {
                LOGGER.error("Form not found: revisionSupervisor.form");
            }
            
            return "Forms deployed to classpath successfully";
        } catch (IOException e) {
            LOGGER.error("Error deploying forms to classpath: {}", e.getMessage());
            return "Error deploying forms to classpath: " + e.getMessage();
        }
    }
}
