package com.miempresa.reembolso.controller;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador para asignar tareas manualmente cuando Tasklist tiene problemas
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskAssignmentController {

    private static final Logger logger = LoggerFactory.getLogger(TaskAssignmentController.class);

    @Autowired
    private ZeebeClient zeebeClient;

    /**
     * Endpoint para listar todas las tareas de usuario disponibles
     */
    @GetMapping("/list")
    public ResponseEntity<?> listTasks() {
        try {
            // Activar todas las tareas de usuario disponibles
            List<ActivatedJob> jobs = zeebeClient.newActivateJobsCommand()
                    .jobType("io.camunda.zeebe:userTask")
                    .maxJobsToActivate(100) // Aumentamos el número máximo de tareas
                    .timeout(Duration.ofMinutes(5)) // Aumentamos el timeout
                    .workerName("manual-assignment-worker")
                    .send()
                    .join()
                    .getJobs();
            
            logger.info("Tareas encontradas: {}", jobs.size());
            if (jobs.isEmpty()) {
                logger.info("No se encontraron tareas activas. Verificando conexión con Zeebe...");
                var topology = zeebeClient.newTopologyRequest().send().join();
                logger.info("Topología de Zeebe: {} brokers, {} particiones", 
                    topology.getBrokers().size(),
                    topology.getBrokers().stream().mapToInt(b -> b.getPartitions().size()).sum());
            } else {
                for (ActivatedJob job : jobs) {
                    logger.info("Tarea encontrada - Key: {}, ElementId: {}, ProcessInstanceKey: {}", 
                        job.getKey(), job.getElementId(), job.getProcessInstanceKey());
                }
            }
            
            // Convertir las tareas a un formato más amigable
            return ResponseEntity.ok(Map.of(
                "tareas", jobs.stream().map(job -> Map.of(
                    "jobKey", job.getKey(),
                    "elementId", job.getElementId(),
                    "processInstanceKey", job.getProcessInstanceKey(),
                    "variables", job.getVariablesAsMap()
                )).toList(),
                "total", jobs.size()
            ));
        } catch (Exception e) {
            logger.error("Error al listar tareas: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error al listar tareas",
                "detalle", e.getMessage()
            ));
        }
    }
    
    /**
     * Endpoint para buscar tareas por ID de instancia de proceso
     */
    @GetMapping("/process/{processInstanceId}")
    public ResponseEntity<?> getTasksByProcessInstanceId(@PathVariable("processInstanceId") String processInstanceId) {
        try {
            logger.info("Buscando tareas para la instancia de proceso: {}", processInstanceId);
            
            // Activar todas las tareas de usuario disponibles
            List<ActivatedJob> jobs = zeebeClient.newActivateJobsCommand()
                    .jobType("io.camunda.zeebe:userTask")
                    .maxJobsToActivate(100)
                    .timeout(Duration.ofMinutes(5))
                    .workerName("process-instance-worker")
                    .send()
                    .join()
                    .getJobs();
            
            // Filtrar manualmente las tareas por ID de instancia de proceso
            List<ActivatedJob> filteredJobs = jobs.stream()
                    .filter(job -> String.valueOf(job.getProcessInstanceKey()).equals(processInstanceId))
                    .collect(Collectors.toList());
            
            logger.info("Tareas encontradas para la instancia {}: {}", processInstanceId, filteredJobs.size());
            
            for (ActivatedJob job : filteredJobs) {
                logger.info("Tarea encontrada - Key: {}, ElementId: {}, ProcessInstanceKey: {}", 
                    job.getKey(), job.getElementId(), job.getProcessInstanceKey());
            }
            
            return ResponseEntity.ok(Map.of(
                "processInstanceId", processInstanceId,
                "tareas", filteredJobs.stream().map(job -> Map.of(
                    "jobKey", job.getKey(),
                    "elementId", job.getElementId(),
                    "processInstanceKey", job.getProcessInstanceKey(),
                    "variables", job.getVariablesAsMap()
                )).toList(),
                "total", filteredJobs.size()
            ));
        } catch (Exception e) {
            logger.error("Error al buscar tareas para la instancia {}: {}", processInstanceId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error al buscar tareas para la instancia " + processInstanceId,
                "detalle", e.getMessage()
            ));
        }
    }
    
    /**
     * Endpoint para asignar una tarea a un usuario
     */
    @PostMapping("/assign/{jobKey}")
    public ResponseEntity<?> assignTask(@PathVariable("jobKey") Long jobKey, @RequestParam("assignee") String assignee) {
        try {
            // Completar la tarea con la variable de asignación
            Map<String, Object> variables = new HashMap<>();
            variables.put("assignee", assignee);
            
            zeebeClient.newCompleteCommand(jobKey)
                    .variables(variables)
                    .send()
                    .join();
            
            logger.info("Tarea {} asignada a {}", jobKey, assignee);
            
            return ResponseEntity.ok(Map.of(
                "mensaje", "Tarea asignada correctamente",
                "jobKey", jobKey,
                "assignee", assignee
            ));
        } catch (Exception e) {
            logger.error("Error al asignar tarea: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error al asignar tarea",
                "detalle", e.getMessage()
            ));
        }
    }
    
    /**
     * Endpoint para completar una tarea con variables
     */
    @PostMapping("/complete/{jobKey}")
    public ResponseEntity<?> completeTask(
            @PathVariable("jobKey") Long jobKey,
            @RequestBody Map<String, Object> variables) {
        
        try {
            logger.info("Completando tarea {} con variables: {}", jobKey, variables);
            
            zeebeClient.newCompleteCommand(jobKey)
                    .variables(variables)
                    .send()
                    .join();
            
            logger.info("Tarea {} completada con éxito", jobKey);
            
            return ResponseEntity.ok(Map.of(
                "mensaje", "Tarea completada correctamente",
                "jobKey", jobKey
            ));
        } catch (Exception e) {
            logger.error("Error al completar tarea {}: {}", jobKey, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error al completar tarea",
                "detalle", e.getMessage()
            ));
        }
    }
}
