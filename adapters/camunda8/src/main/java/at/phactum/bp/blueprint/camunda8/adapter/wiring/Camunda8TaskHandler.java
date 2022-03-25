package at.phactum.bp.blueprint.camunda8.adapter.wiring;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.camunda.bpm.model.xml.ModelInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import at.phactum.bp.blueprint.bpm.deployment.MultiInstance;
import at.phactum.bp.blueprint.bpm.deployment.TaskHandlerBase;
import at.phactum.bp.blueprint.bpm.deployment.parameters.MethodParameter;
import at.phactum.bp.blueprint.camunda8.adapter.deployment.DeploymentService;
import at.phactum.bp.blueprint.camunda8.adapter.wiring.parameters.Camunda8MultiInstanceIndexMethodParameter;
import at.phactum.bp.blueprint.camunda8.adapter.wiring.parameters.Camunda8MultiInstanceTotalMethodParameter;
import at.phactum.bp.blueprint.domain.WorkflowDomainEntity;
import at.phactum.bp.blueprint.service.MultiInstanceElementResolver;
import io.camunda.zeebe.client.api.command.CompleteJobCommandStep1;
import io.camunda.zeebe.client.api.command.FinalCommandStep;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.model.bpmn.instance.Activity;
import io.camunda.zeebe.model.bpmn.instance.BaseElement;
import io.camunda.zeebe.model.bpmn.instance.MultiInstanceLoopCharacteristics;
import io.camunda.zeebe.spring.client.exception.ZeebeBpmnError;
import io.camunda.zeebe.spring.client.jobhandling.CommandWrapper;
import io.camunda.zeebe.spring.client.jobhandling.DefaultCommandExceptionHandlingStrategy;

public class Camunda8TaskHandler extends TaskHandlerBase implements JobHandler {

    private final DefaultCommandExceptionHandlingStrategy commandExceptionHandlingStrategy;

    private final DeploymentService deploymentService;

    public Camunda8TaskHandler(
            final DeploymentService deploymentService,
            final DefaultCommandExceptionHandlingStrategy commandExceptionHandlingStrategy,
            final JpaRepository<WorkflowDomainEntity, String> workflowDomainEntityRepository,
            final Object bean,
            final Method method,
            final List<MethodParameter> parameters) {

        super(workflowDomainEntityRepository, bean, method, parameters);
        this.deploymentService = deploymentService;
        this.commandExceptionHandlingStrategy = commandExceptionHandlingStrategy;

    }

    @Override
    @Transactional
    public void handle(
            final JobClient client,
            final ActivatedJob job) throws Exception {

        CommandWrapper command;
        try {
            final var businessKey = (String) job.getVariablesAsMap().get("id");
            
            Object result = super.execute(
                    businessKey,
                    multiInstanceVariable -> getVariable(job, multiInstanceVariable));

            command = new CommandWrapper(createCompleteCommand(client, job, result), job,
                    commandExceptionHandlingStrategy);
        } catch (ZeebeBpmnError bpmnError) {
            command = new CommandWrapper(createThrowErrorCommand(client, job, bpmnError), job,
                    commandExceptionHandlingStrategy);
        }
        command.executeAsync();

    }
    
    @Override
    protected Object getMultiInstanceElement(
            final String name,
            final Function<String, Object> multiInstanceSupplier) {

        return multiInstanceSupplier
                .apply(name);
        
    }
    
    @Override
    protected Integer getMultiInstanceIndex(
            final String name,
            final Function<String, Object> multiInstanceSupplier) {
        
        return (Integer) multiInstanceSupplier
                .apply(name + Camunda8MultiInstanceIndexMethodParameter.SUFFIX) - 1;
        
    }
    
    @Override
    protected Integer getMultiInstanceTotal(
            final String name,
            final Function<String, Object> multiInstanceSupplier) {
        
        return (Integer) multiInstanceSupplier
                .apply(name + Camunda8MultiInstanceTotalMethodParameter.SUFFIX);
    
    }
    
    @Override
    protected MultiInstance<Object> getMultiInstance(
            final String name,
            final Function<String, Object> multiInstanceSupplier) {
        
        return new MultiInstance<Object>(
                getMultiInstanceElement(name, multiInstanceSupplier),
                getMultiInstanceTotal(name, multiInstanceSupplier),
                getMultiInstanceIndex(name, multiInstanceSupplier));
        
    }
    
    private Object getVariable(
            final ActivatedJob job,
            final String name) {
        
        return job
                .getVariablesAsMap()
                .get(name);
        
    }

    protected Map<String, MultiInstanceElementResolver.MultiInstance<Object>> getMultiInstanceContext(
            final ActivatedJob job,
            final String workflowDomainEntityId) {

        final var result = new LinkedHashMap<String, MultiInstanceElementResolver.MultiInstance<Object>>();

        final var process = deploymentService
                .getProcess(job.getProcessDefinitionKey());

        ModelInstance model = process.getModelInstance();
        String miElement = job.getElementId();
        MultiInstanceLoopCharacteristics loopCharacteristics = null;
        // find multi-instance element from current element up to the root of the
        // process-hierarchy
        while (loopCharacteristics == null) {
            
            // check current element for multi-instance
            final var bpmnElement = model.getModelElementById(miElement);
            if (bpmnElement instanceof Activity) {
                loopCharacteristics = (MultiInstanceLoopCharacteristics) ((Activity) bpmnElement)
                        .getLoopCharacteristics();
            }
            
            // if still not found then check parent
            if (loopCharacteristics == null) {
                miElement = bpmnElement.getParentElement() != null
                        ? ((BaseElement) bpmnElement.getParentElement()).getId()
                        : null;
            }
            // multi-instance found
            else {
                
                result.put(((BaseElement) bpmnElement).getId(),
                        new MultiInstance<Object>(null, -1, -1));
                
            }
            
            // if there is no parent then multi-instance task was used in a
            // non-multi-instance environment
            if ((miElement == null) && (loopCharacteristics == null)) {
                throw new RuntimeException(
                        "No multi-instance context found for element '"
                        + job.getElementId()
                                + "' or its parents! In case of a call-activity this is not supported by ");
            }
            
        }
        
        return result;

    }

    public FinalCommandStep createCompleteCommand(
            final JobClient jobClient,
            final ActivatedJob job,
            final Object result) {

        CompleteJobCommandStep1 completeCommand = jobClient.newCompleteCommand(job.getKey());
        if (result != null) {
            if (result.getClass().isAssignableFrom(Map.class)) {
                completeCommand = completeCommand.variables((Map) result);
            } else if (result.getClass().isAssignableFrom(String.class)) {
                completeCommand = completeCommand.variables((String) result);
            } else if (result.getClass().isAssignableFrom(InputStream.class)) {
                completeCommand = completeCommand.variables((InputStream) result);
            } else {
                completeCommand = completeCommand.variables(result);
            }
        }
        return completeCommand;

    }

    private FinalCommandStep<Void> createThrowErrorCommand(
            final JobClient jobClient,
            final ActivatedJob job,
            final ZeebeBpmnError bpmnError) {

        FinalCommandStep<Void> command = jobClient.newThrowErrorCommand(job.getKey()) // TODO: PR for taking a job only
                                                                                      // in command chain
                .errorCode(bpmnError.getErrorCode()).errorMessage(bpmnError.getErrorMessage());
        return command;

    }

}
