package at.phactum.bp.blueprint.camunda8.adapter.deployment;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.google.common.collect.Streams;

import at.phactum.bp.blueprint.bpm.deployment.ModuleAwareBpmnDeployment;
import at.phactum.bp.blueprint.camunda8.adapter.service.Camunda8ProcessService;
import at.phactum.bp.blueprint.camunda8.adapter.wiring.Camunda8TaskWiring;
import at.phactum.bp.blueprint.utilities.HashCodeInputStream;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.model.bpmn.impl.BpmnModelInstanceImpl;
import io.camunda.zeebe.model.bpmn.impl.BpmnParser;
import io.camunda.zeebe.model.bpmn.instance.BusinessRuleTask;
import io.camunda.zeebe.model.bpmn.instance.EndEvent;
import io.camunda.zeebe.model.bpmn.instance.IntermediateThrowEvent;
import io.camunda.zeebe.model.bpmn.instance.Process;
import io.camunda.zeebe.model.bpmn.instance.SendTask;
import io.camunda.zeebe.model.bpmn.instance.ServiceTask;
import io.camunda.zeebe.model.bpmn.instance.UserTask;
import io.camunda.zeebe.spring.client.ZeebeClientLifecycle;

public class Camunda8DeploymentAdapter extends ModuleAwareBpmnDeployment
        implements Consumer<ZeebeClient> {

	private static final Logger logger = LoggerFactory.getLogger(Camunda8DeploymentAdapter.class);
	
	private final BpmnParser bpmnParser = new BpmnParser();

    private final Camunda8TaskWiring taskWiring;

    private final DeploymentService deploymentService;
	
    private ZeebeClient client;

    public Camunda8DeploymentAdapter(
            final DeploymentService deploymentService,
            final ZeebeClientLifecycle clientLifecycle,
            final Camunda8TaskWiring taskWiring) {
        
        super();
        this.taskWiring = taskWiring;
        this.deploymentService = deploymentService;
        
        clientLifecycle.addStartListener(this);

    }

    @Override
    protected Logger getLogger() {
    	
    	return logger;
    	
    }
    
    @Override
    public void accept(final ZeebeClient client) {

        this.client = client;

        deployAllWorkflowModules();

        taskWiring.openWorkers();

    }

    @Override
    protected void doDeployment(
    		final String workflowModuleId,
            final Resource[] bpmns,
            final Resource[] dmns,
            final Resource[] cmms) throws Exception {

        final var deployProcessCommand = client.newDeployCommand();

        final var deployedProcesses = new HashMap<String, DeployedBpmn>();

        final var deploymentHashCode = new int[] { 0 };
        Arrays
                .stream(bpmns)
                .map(resource -> {
                    try (var inputStream = new HashCodeInputStream(
                            resource.getInputStream(),
                            deploymentHashCode[0])) {
                        
                        logger.info("About to deploy '{}' of workflow-module '{}'",
                                resource.getFilename(), workflowModuleId);
                    	final var model = bpmnParser.parseModelFromStream(inputStream);

                    	final var bpmn = deploymentService.addBpmn(
                                model,
                                inputStream.hashCode(),
                                resource.getDescription());

                        processBpmnModel(workflowModuleId, deployedProcesses, bpmn, model);
                        deploymentHashCode[0] = inputStream.getTotalHashCode();

                    	return deployProcessCommand.addProcessModel(model, resource.getFilename());
                    	
                    } catch (IOException e) {
                        throw new RuntimeException(e.getMessage());
                    }
                })
                .filter(Objects::nonNull)
                .reduce((first, second) -> second)
                .map(command -> command.send().join())
                .orElseThrow()
                .getProcesses()
                .forEach(process -> deploymentService.addProcess(
                        deploymentHashCode[0],
                        process,
                        deployedProcesses.get(process.getBpmnProcessId())));
                
    }
    
    private void processBpmnModel(
            final String workflowModuleId,
            final Map<String, DeployedBpmn> deployedProcesses,
            final DeployedBpmn bpmn,
    		final BpmnModelInstanceImpl model) {

        taskWiring.accept(client);

        final var processService = new Camunda8ProcessService[] { null };

        model.getModelElementsByType(Process.class)
                .stream()
                .filter(Process::isExecutable)
                // wire service port
                .peek(process -> {
                    processService[0] = taskWiring.wireService(workflowModuleId, process.getId());
                    deployedProcesses.put(process.getId(), bpmn);
                })
                // wire task methods
                .flatMap(process -> Streams.concat(
                        taskWiring.connectablesForType(process, model, ServiceTask.class),
                        taskWiring.connectablesForType(process, model, BusinessRuleTask.class),
                        taskWiring.connectablesForType(process, model, SendTask.class),
                        taskWiring.connectablesForType(process, model, UserTask.class),
                        taskWiring.connectablesForType(process, model, IntermediateThrowEvent.class),
                        taskWiring.connectablesForType(process, model, EndEvent.class)))
                .forEach(connectable -> taskWiring.wireTask(processService[0], connectable));
    	
    }
    
}
