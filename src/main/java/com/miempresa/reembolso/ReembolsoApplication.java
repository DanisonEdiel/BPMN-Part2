package com.miempresa.reembolso;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.Deployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableZeebeClient
@Deployment(resources = {"classpath:ToBe.bpmn", "classpath:forms/*.form"})
public class ReembolsoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReembolsoApplication.class, args);
	}

}
