package com.miempresa.reembolso;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * Worker para generar una orden de pago para el reembolso
 */
@Component
public class OrdenPagoDelegate {
    
    private static final Logger logger = LoggerFactory.getLogger(OrdenPagoDelegate.class);
    
    @JobWorker(type = "com.miempresa.reembolso.OrdenPagoDelegate")
    public Map<String, Object> execute(final ActivatedJob job) {
        logger.info("Generando orden de pago para reembolso");
        
        // Obtenemos las variables del proceso
        Map<String, Object> variables = job.getVariablesAsMap();
        
        // Generamos un número de orden único
        String numeroOrden = "OP-" + UUID.randomUUID().toString().substring(0, 8);
        variables.put("numeroOrden", numeroOrden);
        
        // Establecemos la fecha de pago (5 días hábiles desde hoy)
        LocalDate fechaPago = LocalDate.now().plusDays(5);
        variables.put("fechaPago", fechaPago.toString());
        
        // Obtenemos datos para el registro
        Double monto = (Double) variables.get("monto");
        String proveedor = (String) variables.get("proveedor");
        
        logger.info("Orden de pago generada: {} para proveedor {} por monto {}, fecha de pago: {}", 
                numeroOrden, proveedor, monto, fechaPago);
        
        return variables;
    }
}
