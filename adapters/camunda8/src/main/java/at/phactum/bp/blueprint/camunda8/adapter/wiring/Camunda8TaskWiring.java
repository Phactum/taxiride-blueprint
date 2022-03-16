package at.phactum.bp.blueprint.camunda8.adapter.wiring;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;

import at.phactum.bp.blueprint.bpm.deployment.MethodParameter;
import at.phactum.bp.blueprint.bpm.deployment.TaskWiringBase;
import at.phactum.bp.blueprint.camunda8.adapter.deployment.Camunda8DeploymentAdapter;
import at.phactum.bp.blueprint.camunda8.adapter.service.Camunda8ProcessService;
import at.phactum.bp.blueprint.domain.WorkflowDomainEntity;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.model.bpmn.impl.BpmnModelInstanceImpl;
import io.camunda.zeebe.model.bpmn.instance.BaseElement;
import io.camunda.zeebe.model.bpmn.instance.Process;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeTaskDefinition;

public class Camunda8TaskWiring extends TaskWiringBase<Camunda8Connectable, Camunda8ProcessService<?>>
        implements Consumer<ZeebeClient> {

    private final String workerId;
    
    private final ObjectProvider<Camunda8TaskHandler> taskHandlers;

    private final List<Camunda8ProcessService<?>> connectableServices;

    private ZeebeClient client;

    public Camunda8TaskWiring(
            final ApplicationContext applicationContext,
            final String workerId,
            final ObjectProvider<Camunda8TaskHandler> taskHandlers,
            final List<Camunda8ProcessService<?>> connectableServices) {
        
        super(applicationContext);
        this.workerId = workerId;
        this.taskHandlers = taskHandlers;
        this.connectableServices = connectableServices;
        
    }
    
    /**
     * Called by
     * {@link Camunda8DeploymentAdapter#processBpmnModel(BpmnModelInstanceImpl)} to
     * ensure client is available before using wire-methods.
     */
    @Override
    public void accept(
            final ZeebeClient client) {
        
        this.client = client;
        
    }

    public Stream<Camunda8Connectable> connectablesForType(
            final Process process,
            final BpmnModelInstanceImpl model,
            final Class<? extends BaseElement> type) {
        
        return model
                .getModelElementsByType(type)
                .stream()
                .filter(element -> getOwningProcess(element).equals(process))
                .map(element -> new Camunda8Connectable(process, element.getId(),
                        element.getSingleExtensionElement(ZeebeTaskDefinition.class)))
                .filter(connectable -> connectable.isExecutableProcess())
                .filter(connectable -> connectable.getTaskDefinition() != null);
        
    }
    
    private Process getOwningProcess(
            final ModelElementInstance element) {

        if (element instanceof Process) {
            return (Process) element;
        }

        final var parent = element.getParentElement();
        if (parent == null) {
            return null;
        }

        return getOwningProcess(parent);

    }
    
    @Override
    protected <DE extends WorkflowDomainEntity> Camunda8ProcessService<?> connectToBpms(
            final Class<DE> workflowDomainEntityClass,
            final String bpmnProcessId) {
        
        final var processService = connectableServices
                .stream()
                .filter(service -> service.getWorkflowDomainEntityClass().equals(workflowDomainEntityClass))
                .findFirst()
                .get();

        processService.wire(client, bpmnProcessId);

        return processService;
        
    }
    
    @Override
    protected void connectToBpms(
            final Camunda8ProcessService<?> processService,
            final Object bean,
            final Camunda8Connectable connectable,
            final Method method,
            final List<MethodParameter> parameters) {
        
        final var repository = processService.getWorkflowDomainEntityRepository();

        final var taskHandler = taskHandlers.getObject(
                repository,
                connectable.getTaskDefinition(),
                bean,
                method,
                parameters);

        client
                .newWorker()
                .jobType(connectable.getTaskDefinition())
                .handler(taskHandler)
                .name(workerId)
                .fetchVariables(List.of("id"))
                .open();

              // using defaults from config if null, 0 or negative
//              if (zeebeWorkerValue.getName() != null && zeebeWorkerValue.getName().length() > 0) {
//                builder.name(zeebeWorkerValue.getName());
//              } else {
//                builder.name(beanInfo.getBeanName() + "#" + zeebeWorkerValue.getMethodInfo().getMethodName());
//              }
//              if (zeebeWorkerValue.getMaxJobsActive() > 0) {
//                builder.maxJobsActive(zeebeWorkerValue.getMaxJobsActive());
//              }
//              if (zeebeWorkerValue.getTimeout() > 0) {
//                builder.timeout(zeebeWorkerValue.getTimeout());
//              }
//              if (zeebeWorkerValue.getPollInterval() > 0) {
//                builder.pollInterval(Duration.ofMillis(zeebeWorkerValue.getPollInterval()));
//              }
//              if (zeebeWorkerValue.getRequestTimeout() > 0) {
//                builder.requestTimeout(Duration.ofSeconds(zeebeWorkerValue.getRequestTimeout()));
//              }
//              if (zeebeWorkerValue.getFetchVariables().length > 0) {
//                builder.fetchVariables(zeebeWorkerValue.getFetchVariables());
//              }
        
    }

}
