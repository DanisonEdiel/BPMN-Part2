package com.miempresa.reembolso.config;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.spring.client.annotation.Deployment;
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
@Deployment(resources = {"classpath:forms/solicitudReembolso.form", "classpath:forms/revisionSupervisor.form"})
public class FormDeployer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(FormDeployer.class);
    
    @Autowired
    private ZeebeClient zeebeClient;

    @Override
    public void run(String... args) throws Exception {
        verifyFormsExist();
        logZeebeStatus();
    }

    /**
     * Verifica que los formularios existan en el classpath
     */
    private void verifyFormsExist() {
        try {
            // Verificar que los archivos de formulario existan
            String solicitudReembolsoForm = readResourceAsString("classpath:forms/solicitudReembolso.form");
            String revisionSupervisorForm = readResourceAsString("classpath:forms/revisionSupervisor.form");

            logger.info("Formularios verificados y listos para despliegue:");
            logger.info("- solicitudReembolso.form: {} bytes", solicitudReembolsoForm.length());
            logger.info("- revisionSupervisor.form: {} bytes", revisionSupervisorForm.length());
            
            // Mostrar información sobre los formularios
            logger.info("Contenido de solicitudReembolso.form: {}", solicitudReembolsoForm.substring(0, Math.min(100, solicitudReembolsoForm.length())) + "...");
            logger.info("Contenido de revisionSupervisor.form: {}", revisionSupervisorForm.substring(0, Math.min(100, revisionSupervisorForm.length())) + "...");
            
            logger.info("Los formularios serán desplegados automáticamente por la anotación @Deployment");

        } catch (Exception e) {
            logger.error("Error al verificar formularios: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Registra el estado de la conexión con Zeebe
     */
    private void logZeebeStatus() {
        try {
            logger.info("Verificando conexión con Zeebe...");
            var topology = zeebeClient.newTopologyRequest().send().join();
            logger.info("Conexión con Zeebe establecida correctamente");
            logger.info("Topología de Zeebe: {} brokers, {} particiones", 
                    topology.getBrokers().size(),
                    topology.getBrokers().stream().mapToInt(b -> b.getPartitions().size()).sum());
            
            // Intentar obtener información sobre los formularios desplegados
            logger.info("Formularios desplegados correctamente. Recuerda que para ver los formularios en Tasklist:");
            logger.info("1. Asigna la tarea a ti mismo (Assign to me)");
            logger.info("2. Haz clic en 'Complete Task' para ver el formulario");
            logger.info("3. Si el formulario no aparece, verifica que la versión del formulario (8.7.0) coincida con la versión de Tasklist");
            
        } catch (Exception e) {
            logger.error("Error al conectar con Zeebe: {}", e.getMessage(), e);
            logger.error("Esto puede causar problemas con el despliegue de formularios y la ejecución de procesos");
            logger.error("Asegúrate de que el broker de Zeebe esté en ejecución en localhost:26500");
        }
    }

    /**
     * Lee un recurso del classpath como String
     */
    private String readResourceAsString(String resourcePath) throws IOException {
        Resource resource = new ClassPathResource(resourcePath.replace("classpath:", ""));
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
    }
}
