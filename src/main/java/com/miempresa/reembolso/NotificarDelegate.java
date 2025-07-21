package com.miempresa.reembolso;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Worker para enviar notificaciones a los usuarios
 * En un entorno real, este worker se integraría con un sistema de notificaciones
 * (email, SMS, notificaciones push, etc.)
 */
@Component
public class NotificarDelegate {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificarDelegate.class);
    
    @JobWorker(type = "com.miempresa.reembolso.NotificarDelegate")
    public Map<String, Object> execute(final ActivatedJob job) {
        logger.info("Enviando notificación");
        
        // Obtenemos las variables del proceso
        Map<String, Object> variables = job.getVariablesAsMap();
        
        // Determinamos el tipo de notificación según la etapa del proceso
        String activityId = job.getElementId();
        String mensaje = "";
        String destinatario = "";
        
        // Obtenemos datos comunes
        String proveedor = (String) variables.get("proveedor");
        Double monto = (Double) variables.get("monto");
        String numeroFactura = (String) variables.get("numero_factura");
        
        // Determinamos el tipo de notificación según la actividad actual
        if (activityId.contains("Notificar_Error")) {
            destinatario = "empleado";
            mensaje = String.format("Se han detectado errores en la factura %s. Por favor, revise los datos y vuelva a intentar.", 
                                   numeroFactura);
        } else if (activityId.contains("Notificar_Rechazo_Supervisor")) {
            destinatario = "empleado";
            mensaje = String.format("Su solicitud de reembolso para la factura %s por %.2f ha sido rechazada por el supervisor.", 
                                   numeroFactura, monto);
        } else if (activityId.contains("Notificar_Rechazo_Finanzas")) {
            destinatario = "empleado";
            mensaje = String.format("Su solicitud de reembolso para la factura %s por %.2f ha sido rechazada por el departamento de finanzas.", 
                                   numeroFactura, monto);
        } else if (activityId.contains("Notificar_Pago")) {
            destinatario = "empleado";
            String transaccionId = (String) variables.get("transaccionId");
            mensaje = String.format("Su reembolso para la factura %s por %.2f ha sido procesado. ID de transacción: %s", 
                                   numeroFactura, monto, transaccionId);
        } else {
            destinatario = "sistema";
            mensaje = "Notificación genérica del sistema de reembolsos.";
        }
        
        // Registramos la notificación en el log
        logger.info("Notificación enviada a {}: {}", destinatario, mensaje);
        
        // En un entorno real, aquí se enviaría la notificación por el canal adecuado
        // Por ejemplo, envío de email, SMS, notificación push, etc.
        
        // Guardamos la notificación en las variables del proceso y las devolvemos
        variables.put("ultimaNotificacion", mensaje);
        variables.put("destinatarioNotificacion", destinatario);
        
        // Imprimimos el mensaje para simular la notificación
        System.out.println("=================================================================");
        System.out.println("NOTIFICACIÓN PARA: " + destinatario.toUpperCase());
        System.out.println("-----------------------------------------------------------------");
        System.out.println(mensaje);
        System.out.println("=================================================================");
        
        return variables;
    }
}
