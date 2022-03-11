package at.phactum.bp.blueprint.camunda7.adapter.wiring;

import java.lang.reflect.Method;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import at.phactum.bp.blueprint.bpm.deployment.TaskWiringBase;
import at.phactum.bp.blueprint.camunda7.adapter.service.Camunda7ProcessService;
import at.phactum.bp.blueprint.domain.WorkflowDomainEntity;

@Component
public class Camunda7TaskWiring extends TaskWiringBase<Camunda7Connectable> {

    private final ProcessEntityAwareExpressionManager processEntityAwareExpressionManager;

    private final List<Camunda7ProcessService<?>> connectableServices;

    public Camunda7TaskWiring(
            final ApplicationContext applicationContext,
            final ProcessEntityAwareExpressionManager processEntityAwareExpressionManager,
            final List<Camunda7ProcessService<?>> connectableServices) {
        
        super(applicationContext);
        this.processEntityAwareExpressionManager = processEntityAwareExpressionManager;
        this.connectableServices = connectableServices;
        
    }
    
    @Override
    protected void connectToCamunda(
            final Object bean,
            final Camunda7Connectable connectable,
            final Method method) {
        
        final var taskHandler = new Camunda7TaskHandler(bean, method);

        processEntityAwareExpressionManager.addTaskHandler(connectable, taskHandler);

    }
    
    @Override
    protected <DE extends WorkflowDomainEntity> void connectToCamunda(
            final Class<DE> workflowDomainEntityClass,
            final String bpmnProcessId) {
        
        connectableServices
                .stream()
                .filter(service -> service.getWorkflowDomainEntityClass().equals(workflowDomainEntityClass))
                .forEach(service -> service.wire(bpmnProcessId));
        
    }

}
