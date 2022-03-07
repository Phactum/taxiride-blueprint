package at.phactum.bp.blueprint.camunda7.adapter;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableProcessApplication
public class Camunda7AdapterConfiguration {

    @Bean
    public Camunda7DeploymentAdapter camunda7Adapter() {

        return new Camunda7DeploymentAdapter();

    }

}
