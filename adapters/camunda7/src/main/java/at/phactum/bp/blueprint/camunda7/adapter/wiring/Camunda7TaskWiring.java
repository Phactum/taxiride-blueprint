package at.phactum.bp.blueprint.camunda7.adapter.wiring;

import java.lang.reflect.Method;

import org.springframework.stereotype.Component;

import at.phactum.bp.blueprint.bpm.deployment.TaskWiringBase;
import at.phactum.bp.blueprint.domain.WorkflowDomainEntity;

@Component
public class Camunda7TaskWiring extends TaskWiringBase<Camunda7Connectable> {
    
    @Override
    protected void connectToCamunda(
            final Object bean,
            final Camunda7Connectable connectable,
            final Method method) {
        
    }
    
    @Override
    protected <DE extends WorkflowDomainEntity> void connectToCamunda(
            final Class<DE> workflowDomainEntityClass,
            final String bpmnProcessId) {
        
        
    }

}
