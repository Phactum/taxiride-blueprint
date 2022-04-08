package at.phactum.bp.blueprint.camunda7.adapter.wiring;

import java.lang.reflect.Method;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import at.phactum.bp.blueprint.bpm.deployment.TaskWiringBase;
import at.phactum.bp.blueprint.bpm.deployment.parameters.MethodParameter;
import at.phactum.bp.blueprint.camunda7.adapter.service.Camunda7ProcessService;
import at.phactum.bp.blueprint.process.ProcessService;

@Component
public class Camunda7TaskWiring extends TaskWiringBase<Camunda7Connectable, Camunda7ProcessService<?>> {

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
    protected void connectToBpms(
            final Camunda7ProcessService<?> processService,
            final Object bean,
            final Camunda7Connectable connectable,
            final Method method,
            final List<MethodParameter> parameters) {
        
        final var repository = processService.getWorkflowDomainEntityRepository();
        
        @SuppressWarnings("unchecked")
        final var taskHandler = new Camunda7TaskHandler(
                (JpaRepository<Object, String>) repository,
                bean,
                method,
                parameters);

        processEntityAwareExpressionManager.addTaskHandler(connectable, taskHandler);

    }
    
    @Override
    protected <DE> Camunda7ProcessService<?> connectToBpms(
            final String workflowModuleId,
            final Class<DE> workflowDomainEntityClass,
            final String bpmnProcessId) {
        
        final var processService = connectableServices
                .stream()
                .filter(service -> service.getWorkflowDomainEntityClass().equals(workflowDomainEntityClass))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "You need to autowire '"
                        + ProcessService.class.getName()
                        + "<"
                        + workflowDomainEntityClass.getName()
                        + ">' in your code to be able to start workflows!"));

        processService.wire(workflowModuleId, bpmnProcessId);

        return processService;
        
    }

}
