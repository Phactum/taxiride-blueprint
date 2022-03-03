package at.phactum.bp.blueprint.camunda8.adapter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.camunda.zeebe.spring.client.EnableZeebeClient;

@Configuration
@EnableZeebeClient
public class Camunda8AdapterConfiguration {

	@Bean
	public Camunda8DeploymentAdapter camunda8Adapter() {
		
		return new Camunda8DeploymentAdapter();
		
	}
	
}
