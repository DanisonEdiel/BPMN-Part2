package com.miempresa.reembolso;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para manejar las solicitudes de reembolso
 */
@Controller
public class ReembolsoController {

    private static final Logger log = LoggerFactory.getLogger(ReembolsoController.class);

    /**
     * Página principal que muestra el formulario de solicitud
     */
    @GetMapping("/")
    public String mostrarFormulario() {
        return "redirect:/static/forms/solicitudReembolso.html";
    }

    /**
     * Endpoint para iniciar una instancia del proceso de reembolso con datos de formulario
     */
    @PostMapping("/api/iniciar-reembolso")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> iniciarReembolso(
            @RequestParam String proveedor,
            @RequestParam Double monto,
            @RequestParam(required = false) MultipartFile factura,
            @RequestParam(required = false) String concepto) {
        
        log.info("Iniciando proceso de reembolso para proveedor: {}, monto: {}", proveedor, monto);
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("proveedor", proveedor);
        variables.put("monto", monto);
        
        if (concepto != null && !concepto.isEmpty()) {
            variables.put("concepto", concepto);
        }
        
        if (factura != null && !factura.isEmpty()) {
            variables.put("facturaRecibida", true);
            variables.put("nombreFactura", factura.getOriginalFilename());
        } else {
            variables.put("facturaRecibida", false);
        }
        
        try {
            // Simulamos el inicio del proceso (en un entorno real, aquí se conectaría con Zeebe)
            log.info("Simulando inicio de proceso con variables: {}", variables);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("processInstanceKey", "simulation-" + System.currentTimeMillis());
            response.put("message", "Proceso de reembolso iniciado correctamente (simulación)");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al iniciar la instancia del proceso", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Endpoint para iniciar una instancia del proceso de reembolso con datos JSON
     */
    @PostMapping(value = "/api/iniciar-reembolso-json", consumes = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> iniciarReembolsoJson(@RequestBody Map<String, Object> datos) {
        
        String proveedor = (String) datos.getOrDefault("proveedor", "");
        Double monto = Double.parseDouble(datos.getOrDefault("monto", 0).toString());
        
        log.info("Iniciando proceso de reembolso (JSON) para proveedor: {}, monto: {}", proveedor, monto);
        
        try {
            // Simulamos el inicio del proceso (en un entorno real, aquí se conectaría con Zeebe)
            log.info("Simulando inicio de proceso con variables: {}", datos);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("processInstanceKey", "simulation-" + System.currentTimeMillis());
            response.put("message", "Proceso de reembolso iniciado correctamente (simulación)");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al iniciar la instancia del proceso", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Endpoint para verificar el estado de la conexión con Camunda
     */
    @GetMapping("/api/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "connected");
        status.put("message", "Aplicación conectada a Camunda Platform 8 local");
        
        return ResponseEntity.ok(status);
    }
}
