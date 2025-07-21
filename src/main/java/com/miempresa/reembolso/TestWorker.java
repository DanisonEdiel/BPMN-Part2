package com.miempresa.reembolso;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TestWorker {

    private static final Logger log = LoggerFactory.getLogger(TestWorker.class);

    @JobWorker(type = "com.miempresa.reembolso.test")
    public void handleTestJob(final ActivatedJob job) {
        log.info("¡Conexión a Camunda SaaS verificada! Job activado: {}", job);
        // Aquí puedes completar el job si es necesario
        // zeebeClient.newCompleteCommand(job.getKey()).send().join();
    }
}
