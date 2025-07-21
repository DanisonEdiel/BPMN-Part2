package com.miempresa.reembolso;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Worker para validar si hay presupuesto disponible para el reembolso
 */
@Component
public class ValidarPresupuestoDelegate {
    
    private static final Logger logger = LoggerFactory.getLogger(ValidarPresupuestoDelegate.class);
    
    // Simulación de presupuestos disponibles por departamento
    private static final Map<String, Double> PRESUPUESTOS = new HashMap<>();
    static {
        PRESUPUESTOS.put("VENTAS", 5000.0);
        PRESUPUESTOS.put("MARKETING", 3000.0);
        PRESUPUESTOS.put("IT", 8000.0);
        PRESUPUESTOS.put("RRHH", 2000.0);
        PRESUPUESTOS.put("FINANZAS", 4000.0);
        PRESUPUESTOS.put("OPERACIONES", 6000.0);
    }
    
    @JobWorker(type = "com.miempresa.reembolso.ValidarPresupuestoDelegate")
    public Map<String, Object> execute(final ActivatedJob job) {
        logger.info("Iniciando validación de presupuesto");
        
        // Obtenemos las variables del proceso
        Map<String, Object> variables = job.getVariablesAsMap();
        
        // Obtenemos los datos necesarios para la validación
        Double monto = (Double) variables.get("monto");
        String departamento = (String) variables.get("departamento");
        
        // Si no hay departamento especificado, usamos uno por defecto
        if (departamento == null || departamento.isEmpty()) {
            departamento = "OPERACIONES";
            variables.put("departamento", departamento);
            logger.info("Departamento no especificado, usando departamento por defecto: {}", departamento);
        }
        
        // Convertimos a mayúsculas para normalizar
        departamento = departamento.toUpperCase();
        
        // Verificamos si hay presupuesto disponible
        boolean presupuestoAprobado = verificarPresupuesto(departamento, monto);
        variables.put("presupuestoAprobado", presupuestoAprobado);
        
        if (presupuestoAprobado) {
            logger.info("Presupuesto aprobado para departamento {} por monto {}", departamento, monto);
        } else {
            logger.warn("Presupuesto rechazado para departamento {} por monto {}", departamento, monto);
            variables.put("motivoRechazo", "Presupuesto insuficiente para el departamento " + departamento);
        }
        
        return variables;
    }
    
    /**
     * Verifica si hay presupuesto disponible para el departamento y monto especificados
     */
    private boolean verificarPresupuesto(String departamento, Double monto) {
        // Si no hay monto, no podemos validar
        if (monto == null) {
            logger.warn("No se puede validar presupuesto sin monto");
            return false;
        }
        
        // Obtenemos el presupuesto disponible para el departamento
        Double presupuestoDisponible = PRESUPUESTOS.getOrDefault(departamento, 0.0);
        
        // Verificamos si hay suficiente presupuesto
        return monto <= presupuestoDisponible;
    }
}
