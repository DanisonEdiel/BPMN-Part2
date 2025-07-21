package com.miempresa.reembolso;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

/**
 * Worker para procesar el pago del reembolso
 */
@Component
public class PagoDelegate {
    
    private static final Logger logger = LoggerFactory.getLogger(PagoDelegate.class);
    
    @JobWorker(type = "com.miempresa.reembolso.PagoDelegate")
    public Map<String, Object> execute(final ActivatedJob job) {
        logger.info("Procesando pago de reembolso");
        
        // Obtenemos las variables del proceso
        Map<String, Object> variables = job.getVariablesAsMap();
        
        // Generamos un ID de transacción único
        String idTransaccion = "TX-" + UUID.randomUUID().toString().substring(0, 8);
        variables.put("idTransaccion", idTransaccion);
        
        // Establecemos la fecha y hora de la transacción
        LocalDateTime fechaTransaccion = LocalDateTime.now();
        String fechaTransaccionStr = fechaTransaccion.format(DateTimeFormatter.ISO_DATE_TIME);
        variables.put("fechaTransaccion", fechaTransaccionStr);
        
        // Simulamos el procesamiento del pago
        try {
            // Simulamos un pequeño retraso para el procesamiento
            Thread.sleep(1000);
            
            // Obtenemos datos para el registro
            Double monto = (Double) variables.get("monto");
            String numeroOrden = (String) variables.get("numeroOrden");
            
            // Establecemos que el pago fue exitoso
            variables.put("pagoExitoso", true);
            
            logger.info("Pago procesado exitosamente: Transacción {}, Orden {}, Monto {}, Fecha {}", 
                    idTransaccion, numeroOrden, monto, fechaTransaccionStr);
            
        } catch (Exception e) {
            logger.error("Error al procesar el pago: {}", e.getMessage(), e);
            variables.put("pagoExitoso", false);
            variables.put("errorPago", e.getMessage());
        }
        
        return variables;
    }
}
