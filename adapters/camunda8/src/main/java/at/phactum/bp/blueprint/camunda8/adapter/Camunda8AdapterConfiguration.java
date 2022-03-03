package at.phactum.bp.blueprint.camunda8.adapter;

import java.lang.reflect.Method;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.ZeebeClientLifecycle;
import io.camunda.zeebe.spring.client.jobhandling.DefaultCommandExceptionHandlingStrategy;

@Configuration
@EnableZeebeClient
public class Camunda8AdapterConfiguration {

    @Autowired
    private ZeebeClientLifecycle clientLifecycle;
    
    @Autowired
    private DefaultCommandExceptionHandlingStrategy commandExceptionHandlingStrategy;

    @Bean
    public Camunda8DeploymentAdapter camunda8Adapter() {

        final var result = new Camunda8DeploymentAdapter();

        clientLifecycle.addStartListener(result);

        return result;

    }

    @Bean
    public Camunda8TaskWiring camunda8TaskWiring() {

        return new Camunda8TaskWiring();

    }
    
    @Scope("prototype")
    @Bean
    public Camunda8TaskHandler camunda8TaskHandler(
            final String taskDefinition,
            final Object bean,
            final Method method) {
        
        return new Camunda8TaskHandler(commandExceptionHandlingStrategy, bean, method);
        
    }

}
