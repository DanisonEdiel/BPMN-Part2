package com.miempresa.reembolso;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Worker para validar los datos de la solicitud de reembolso
 * Compara los datos ingresados en el formulario con los extraídos por OCR
 */
@Component
public class ValidarDatosDelegate {
    
    private static final Logger logger = LoggerFactory.getLogger(ValidarDatosDelegate.class);
    
    @JobWorker(type = "com.miempresa.reembolso.ValidarDatosDelegate")
    public Map<String, Object> execute(final ActivatedJob job) {
        logger.info("Iniciando validación de datos");
        
        // Obtenemos las variables del proceso
        Map<String, Object> variables = job.getVariablesAsMap();
        
        // Lista para almacenar errores encontrados
        List<String> errores = new ArrayList<>();
        
        // Validamos los datos básicos
        validarDatosBasicos(variables, errores);
        
        // Validamos la fecha de la factura
        validarFechaFactura(variables, errores);
        
        // Validamos el monto de la factura
        validarMonto(variables, errores);
        
        // Determinamos si los datos son válidos
        boolean datosValidos = errores.isEmpty();
        variables.put("datosValidos", datosValidos);
        
        // Si hay errores, los guardamos en una variable del proceso
        if (!datosValidos) {
            variables.put("errores", String.join(", ", errores));
            logger.warn("Validación fallida: {}", errores);
        } else {
            logger.info("Validación exitosa");
            
            // Determinamos si requiere revisión humana basado en el monto
            Double monto = (Double) variables.get("monto");
            boolean requiereRevisionHumana = monto > 1000.0;
            variables.put("requiereRevisionHumana", requiereRevisionHumana);
            
            if (requiereRevisionHumana) {
                logger.info("El monto {} excede el límite de aprobación automática, requiere revisión humana", monto);
            } else {
                logger.info("El monto {} está dentro del límite de aprobación automática", monto);
            }
        }
        
        return variables;
    }
    
    /**
     * Valida los datos básicos de la solicitud
     */
    private void validarDatosBasicos(Map<String, Object> variables, List<String> errores) {
        // Validamos que existan los datos obligatorios
        if (variables.get("proveedor") == null || ((String)variables.get("proveedor")).isEmpty()) {
            errores.add("El nombre del proveedor es obligatorio");
        }
        
        if (variables.get("numero_factura") == null || ((String)variables.get("numero_factura")).isEmpty()) {
            errores.add("El número de factura es obligatorio");
        }
        
        if (variables.get("concepto") == null || ((String)variables.get("concepto")).isEmpty()) {
            errores.add("El concepto del gasto es obligatorio");
        }
        
        // Validamos formato del número de factura (debe tener al menos un guion)
        String numeroFactura = (String) variables.get("numero_factura");
        if (numeroFactura != null && !numeroFactura.contains("-") && !numeroFactura.matches("F\\d+")) {
            errores.add("El número de factura debe tener formato serie-número (ej: F001-12345)");
        }
    }
    
    /**
     * Valida la fecha de la factura
     */
    private void validarFechaFactura(Map<String, Object> variables, List<String> errores) {
        String fechaFactura = (String) variables.get("fecha_factura");
        
        if (fechaFactura == null || fechaFactura.isEmpty()) {
            errores.add("La fecha de la factura es obligatoria");
            return;
        }
        
        try {
            // Intentamos parsear la fecha
            LocalDate fecha = LocalDate.parse(fechaFactura);
            LocalDate hoy = LocalDate.now();
            
            // La fecha no puede ser futura
            if (fecha.isAfter(hoy)) {
                errores.add("La fecha de la factura no puede ser futura");
            }
            
            // La factura no puede ser de más de 3 meses atrás
            if (fecha.isBefore(hoy.minusMonths(3))) {
                errores.add("La factura no puede ser de más de 3 meses de antigüedad");
            }
            
        } catch (DateTimeParseException e) {
            errores.add("El formato de la fecha de factura es incorrecto, debe ser YYYY-MM-DD");
        }
    }
    
    /**
     * Valida el monto de la factura
     */
    private void validarMonto(Map<String, Object> variables, List<String> errores) {
        Double monto = (Double) variables.get("monto");
        
        if (monto == null) {
            errores.add("El monto de la factura es obligatorio");
            return;
        }
        
        // El monto debe ser positivo
        if (monto <= 0) {
            errores.add("El monto debe ser mayor que cero");
        }
        
        // El monto no puede exceder un límite máximo (por ejemplo, 10000)
        if (monto > 10000) {
            errores.add("El monto excede el límite máximo permitido de 10,000");
        }
    }
}
