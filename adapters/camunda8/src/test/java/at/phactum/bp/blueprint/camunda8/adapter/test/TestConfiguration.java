package at.phactum.bp.blueprint.camunda8.adapter.test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import at.phactum.bp.blueprint.camunda8.adapter.Camunda8DeploymentAdapter;
import at.phactum.bp.blueprint.camunda8.adapter.Camunda8TaskWiring;

@Configuration
@ComponentScan(basePackageClasses = { TestConfiguration.class })
class TestConfiguration {

    @Bean
    public Camunda8DeploymentAdapter camunda8Adapter() {

        return new Camunda8DeploymentAdapter();

    }

    @Bean
    public Camunda8TaskWiring camunda8TaskWiring() {

        return new Camunda8TaskWiring();

    }

}
