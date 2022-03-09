package at.phactum.bp.blueprint.camunda7.adapter;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import at.phactum.bp.blueprint.camunda7.adapter.deployment.Camunda7DeploymentAdapter;
import at.phactum.bp.blueprint.camunda7.adapter.wiring.Camunda7TaskWiring;
import at.phactum.bp.blueprint.camunda7.adapter.wiring.Camunda7TaskWiringPlugin;
import at.phactum.bp.blueprint.camunda7.adapter.wiring.TaskWiringBpmnParseListener;

@Configuration
@EnableProcessApplication
public class Camunda7AdapterConfiguration {

    @Autowired
    private ProcessEngine processEngine;

    @Bean
    public Camunda7DeploymentAdapter camunda7Adapter() {

        return new Camunda7DeploymentAdapter(processEngine);

    }
    
    @Bean
    public Camunda7TaskWiring taskWiring() {
        
        return new Camunda7TaskWiring();
        
    }
    
    @Bean
    public TaskWiringBpmnParseListener taskWiringBpmnParseListener(
            final Camunda7TaskWiring taskWiring) {
        
        return new TaskWiringBpmnParseListener(taskWiring);
        
    }
    
    @Bean
    public Camunda7TaskWiringPlugin taskWiringCamundaPlugin(
            final TaskWiringBpmnParseListener taskWiringBpmnParseListener) {
        
        return new Camunda7TaskWiringPlugin(taskWiringBpmnParseListener);
        
    }

}
