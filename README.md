# Sistema de Reembolso Automatizado con Camunda 8

Este proyecto implementa un proceso de reembolso automatizado utilizando Camunda 8 Platform, Spring Boot y un servicio OCR externo.

## Arquitectura

El sistema consta de los siguientes componentes:

1. **Proceso BPMN**: Define el flujo de trabajo del reembolso (`ToBe.bpmn`)
2. **Formularios Camunda**: Formularios para la interacción con usuarios (`solicitudReembolso.form`, `revisionSupervisor.form`)
3. **Servicio OCR**: Microservicio externo en `http://localhost:5000/ocr` para extraer datos de facturas
4. **Aplicación Spring Boot**: Integra todos los componentes y se conecta con Camunda 8

## Flujo del Proceso

1. El usuario completa el formulario "solicitudReembolso" que incluye la carga de una factura
2. El sistema envía la factura al servicio OCR en `http://localhost:5000/ocr` (POST)
3. El OCR extrae los datos y los devuelve al proceso
4. El proceso continúa con la validación y aprobación por supervisores

## Requisitos

- Java 17 o superior
- Maven
- Docker y Docker Compose
- Camunda 8 Platform (Zeebe, Operate, Tasklist)
- Servicio OCR en el puerto 5000

## Configuración

### Camunda 8 Platform

El proyecto está configurado para conectarse a Camunda 8 con los siguientes parámetros:

- Zeebe Broker: `localhost:26500`
- Operate UI: `http://localhost:8081`
- Tasklist UI: `http://localhost:8082`

### Aplicación Spring Boot

- Puerto: 8090
- Configuración en `application.properties`

## Despliegue de Formularios

Los formularios se despliegan de dos maneras:

1. **Automáticamente**: A través del `FormDeployer` que utiliza ZeebeClient para desplegar los formularios programáticamente al iniciar la aplicación.

2. **Manualmente**: Usando Camunda Modeler:
   - Abrir el formulario en Camunda Modeler
   - Clic en "Deploy"
   - Configurar:
     - Deployment name: `solicitudReembolso` (o el nombre del formulario)
     - REST endpoint: `http://localhost:8080`
   - Clic en "Deploy"

## Servicio OCR

El servicio OCR externo debe estar disponible en `http://localhost:5000/ocr`. Este servicio:

- Recibe: JSON con la imagen en base64 en la propiedad "image"
- Devuelve: JSON con los datos extraídos (proveedor, monto, fecha, etc.)

## Ejecución

1. Iniciar Camunda 8 Platform con Docker:
   ```
   docker-compose up -d
   ```

2. Compilar y ejecutar la aplicación:
   ```
   ./mvnw clean package
   ./mvnw spring-boot:run
   ```

3. Acceder a las interfaces:
   - Aplicación: `http://localhost:8090`
   - Operate: `http://localhost:8081`
   - Tasklist: `http://localhost:8082`

## Solución de Problemas

### Formularios no encontrados

Si los formularios no se encuentran durante la ejecución del proceso:

1. Verificar que los IDs de los formularios en los archivos `.form` coincidan con los referenciados en el BPMN
2. Asegurarse de que los formularios estén desplegados correctamente
3. Revisar los logs para errores de despliegue

### Problemas con el servicio OCR

Si el servicio OCR no responde:

1. Verificar que el servicio esté en ejecución en `http://localhost:5000`
2. El `OcrDelegate` está configurado para simular datos si el servicio no está disponible

## Clases Principales

- `ReembolsoApplication`: Punto de entrada de la aplicación
- `OcrDelegate`: Worker que procesa las facturas con OCR
- `FormDeployer`: Despliega formularios programáticamente
- `FormDeploymentConfig`: Copia archivos de formulario al classpath

## Configuración Avanzada

Para personalizar la configuración, editar `application.properties`:

```properties
# Zeebe Client
zeebe.client.broker.gateway-address=localhost:26500
zeebe.client.security.plaintext=true

# Server
server.port=8090

# Formularios
camunda.bpm.generic-properties.properties.userTaskForm.enabled=true
camunda.bpm.generic-properties.properties.userTaskForm.resourcePattern=classpath*:/forms/*.form
```
