package com.miempresa.reembolso.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * Configuration class for verifying form availability at startup.
 */
@Configuration
public class CamundaFormConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(CamundaFormConfig.class);

    @Autowired
    private ResourceLoader resourceLoader;

    @Bean
    public CommandLineRunner logFormAvailability() {
        return args -> {
            try {
                // Load form files
                Resource solicitudForm = resourceLoader.getResource("classpath:forms/solicitudReembolso.form");
                Resource revisionForm = resourceLoader.getResource("classpath:forms/revisionSupervisor.form");

                if (solicitudForm.exists() && revisionForm.exists()) {
                    // Read form content to verify they're accessible
                    String solicitudFormContent = resourceToString(solicitudForm);
                    String revisionFormContent = resourceToString(revisionForm);

                    LOGGER.info("Form files are available and accessible:");
                    LOGGER.info("solicitudReembolso.form - ID: {}", extractFormId(solicitudFormContent));
                    LOGGER.info("revisionSupervisor.form - ID: {}", extractFormId(revisionFormContent));
                    LOGGER.info("Forms are ready to be deployed with the BPMN process");
                } else {
                    LOGGER.error("Form files not found!");
                    if (!solicitudForm.exists()) {
                        LOGGER.error("Missing: solicitudReembolso.form");
                    }
                    if (!revisionForm.exists()) {
                        LOGGER.error("Missing: revisionSupervisor.form");
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Error checking form availability: {}", e.getMessage(), e);
            }
        };
    }

    private String resourceToString(Resource resource) throws IOException {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
    }
    
    private String extractFormId(String formContent) {
        // Simple extraction of form ID from JSON content
        if (formContent.contains("\"id\":")) {
            int idIndex = formContent.indexOf("\"id\":");
            if (idIndex > 0) {
                int startQuote = formContent.indexOf("\"", idIndex + 5);
                int endQuote = formContent.indexOf("\"", startQuote + 1);
                if (startQuote > 0 && endQuote > 0) {
                    return formContent.substring(startQuote + 1, endQuote);
                }
            }
        }
        return "unknown";
    }
}
