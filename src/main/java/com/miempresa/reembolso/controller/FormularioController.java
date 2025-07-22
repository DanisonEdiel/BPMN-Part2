package com.miempresa.reembolso.controller;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para manejar formularios y procesos de reembolso
 */
@RestController
@RequestMapping("/api")
public class FormularioController {

    private static final Logger logger = LoggerFactory.getLogger(FormularioController.class);

    @Autowired
    private ZeebeClient zeebeClient;

    /**
     * Endpoint para iniciar una nueva instancia del proceso de reembolso
     */
    @PostMapping("/iniciar-proceso")
    public ResponseEntity<?> iniciarProceso() {
        try {
            ProcessInstanceEvent instance = zeebeClient.newCreateInstanceCommand()
                    .bpmnProcessId("Process_1ruc8ds")
                    .latestVersion()
                    .send()
                    .join();
            
            logger.info("Proceso iniciado con ID: {}", instance.getProcessInstanceKey());
            
            return ResponseEntity.ok(Map.of(
                "mensaje", "Proceso iniciado correctamente",
                "processInstanceId", instance.getProcessInstanceKey()
            ));
        } catch (Exception e) {
            logger.error("Error al iniciar el proceso: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error al iniciar el proceso",
                "detalle", e.getMessage()
            ));
        }
    }

    /**
     * Endpoint para enviar el formulario de solicitud de reembolso
     */
    @PostMapping("/enviar-formulario")
    public ResponseEntity<?> enviarFormulario(
            @RequestParam("empleado") String empleado,
            @RequestParam("departamento") String departamento,
            @RequestParam("monto") Double monto,
            @RequestParam("concepto") String concepto,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("factura") MultipartFile factura) {
        
        try {
            // Convertir la factura a Base64
            String facturaBase64 = Base64.getEncoder().encodeToString(factura.getBytes());
            
            // Buscar la tarea activa para el usuario
            // En un entorno real, necesitarías identificar la tarea específica
            // Aquí simulamos completar la tarea directamente
            
            Map<String, Object> variables = new HashMap<>();
            variables.put("empleado", empleado);
            variables.put("departamento", departamento);
            variables.put("monto", monto);
            variables.put("concepto", concepto);
            variables.put("descripcion", descripcion);
            variables.put("facturaFile", facturaBase64);
            
            // Iniciar un nuevo proceso con las variables
            ProcessInstanceEvent instance = zeebeClient.newCreateInstanceCommand()
                    .bpmnProcessId("Process_1ruc8ds")
                    .latestVersion()
                    .variables(variables)
                    .send()
                    .join();
            
            logger.info("Formulario enviado y proceso iniciado con ID: {}", instance.getProcessInstanceKey());
            
            return ResponseEntity.ok(Map.of(
                "mensaje", "Formulario enviado correctamente",
                "processInstanceId", instance.getProcessInstanceKey()
            ));
            
        } catch (IOException e) {
            logger.error("Error al procesar el archivo: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error al procesar el archivo",
                "detalle", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Error al enviar el formulario: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error al enviar el formulario",
                "detalle", e.getMessage()
            ));
        }
    }
    
    /**
     * Endpoint para completar una tarea específica
     */
    @PostMapping("/completar-tarea/{jobKey}")
    public ResponseEntity<?> completarTarea(
            @PathVariable("jobKey") Long jobKey,
            @RequestBody Map<String, Object> variables) {
        
        try {
            // En Zeebe, necesitamos pasar el jobKey como parámetro al método newCompleteCommand
            zeebeClient.newCompleteCommand(jobKey)
                    .variables(variables)
                    .send()
                    .join();
            
            logger.info("Tarea completada: {}", jobKey);
            
            return ResponseEntity.ok(Map.of(
                "mensaje", "Tarea completada correctamente"
            ));
        } catch (Exception e) {
            logger.error("Error al completar la tarea: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error al completar la tarea",
                "detalle", e.getMessage()
            ));
        }
    }
    
    /**
     * Endpoint para completar una tarea de usuario directamente sin usar Tasklist
     * Útil cuando hay problemas de conectividad entre Tasklist y Zeebe
     */
    @PostMapping("/completar-tarea-directo/{processInstanceId}")
    public ResponseEntity<?> completarTareaDirecto(
            @PathVariable("processInstanceId") String processInstanceId,
            @RequestParam(value = "facturaBase64", required = false) String facturaBase64,
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "monto", required = false) String monto,
            @RequestParam(value = "concepto", required = false) String concepto,
            @RequestParam(value = "fecha", required = false) String fecha) {

        try {
            logger.info("Completando tarea para la instancia de proceso: {}", processInstanceId);
            
            // Crear variables para completar la tarea
            Map<String, Object> variables = new HashMap<>();
            
            // Añadir variables solo si no son nulas
            if (nombre != null) variables.put("nombre", nombre);
            if (monto != null) variables.put("monto", monto);
            if (concepto != null) variables.put("concepto", concepto);
            if (fecha != null) variables.put("fecha", fecha);
            if (facturaBase64 != null) variables.put("facturaFile", facturaBase64);
            
            // Completar la tarea directamente usando el ID de instancia de proceso
            zeebeClient.newSetVariablesCommand(Long.parseLong(processInstanceId))
                    .variables(variables)
                    .send()
                    .join();
            
            logger.info("Variables establecidas para la instancia {}: {}", processInstanceId, variables.keySet());
            
            // Publicar mensaje para avanzar el flujo
            zeebeClient.newPublishMessageCommand()
                    .messageName("FormularioCompletado")
                    .correlationKey(processInstanceId)
                    .variables(variables)
                    .send()
                    .join();
            
            logger.info("Mensaje publicado para avanzar el flujo de la instancia: {}", processInstanceId);
            
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Tarea completada correctamente",
                    "processInstanceId", processInstanceId,
                    "variables", variables.keySet()
            ));
        } catch (Exception e) {
            logger.error("Error al completar tarea para la instancia {}: {}", processInstanceId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Error al completar tarea",
                    "detalle", e.getMessage()
            ));
        }
    }
}
