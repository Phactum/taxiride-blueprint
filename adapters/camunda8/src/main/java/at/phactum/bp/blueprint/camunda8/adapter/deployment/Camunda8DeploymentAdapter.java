package at.phactum.bp.blueprint.camunda8.adapter.deployment;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import at.phactum.bp.blueprint.bpm.deployment.ModuleAwareBpmnDeployment;
import at.phactum.bp.blueprint.camunda8.adapter.service.Camunda8ProcessService;
import at.phactum.bp.blueprint.camunda8.adapter.wiring.Camunda8TaskWiring;
import at.phactum.bp.blueprint.modules.WorkflowModuleIdAwareProperties;
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

@Transactional
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
            final WorkflowModuleIdAwareProperties properties,
            final Resource[] bpmns,
            final Resource[] dmns,
            final Resource[] cmms) throws Exception {

        final var deploymentHashCode = new int[] { 0 };

        final var deployResourceCommand = client.newDeployResourceCommand();

        // Add all DMNs to deploy-command: on one hand to deploy them and on the
        // other hand to consider their hash code on calculating total package hash code
        Arrays
                .stream(dmns)
                .forEach(resource -> {
                    try (var inputStream = new HashCodeInputStream(
                            resource.getInputStream(),
                            deploymentHashCode[0])) {
                        
                        final var bytes = StreamUtils.copyToByteArray(inputStream);
                        
                        deploymentHashCode[0] = inputStream.getTotalHashCode();
                        
                        deployResourceCommand.addResourceBytes(bytes, resource.getFilename());
                        
                    } catch (IOException e) {
                        throw new RuntimeException(e.getMessage());
                    }
                });
        
        final var deployedProcesses = new HashMap<String, DeployedBpmn>();

        // Add all BPMNs to deploy-command: on one hand to deploy them and on the
        // other hand to wire them to the using project beans according to the SPI
        final var deploymentCommand = Arrays
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

                    	return deployResourceCommand.addProcessModel(model, resource.getFilename());
                    	
                    } catch (IOException e) {
                        throw new RuntimeException(e.getMessage());
                    }
                })
                .filter(Objects::nonNull)
                .reduce((first, second) -> second);
        
        final var deployedResources = deploymentCommand
                .map(command -> command.send().join())
                .orElseThrow();
                
        // BPMNs which are part of the current package will stored
        deployedResources
                .getProcesses()
                .stream()
                .map(process -> deploymentService.addProcess(
                        deploymentHashCode[0],
                        process,
                        deployedProcesses.get(process.getBpmnProcessId())).getDefinitionKey())
                .collect(Collectors.toList());
        
        // BPMNs which were deployed in the past need to be forced to be parsed for wiring
        deploymentService
                .getBpmnNotOfPackage(deploymentHashCode[0])
                .stream()
                .forEach(bpmn -> {
                    
                    try (var inputStream = new ByteArrayInputStream(
                            bpmn.getResource())) {
                        
                        logger.info("About to verify old BPMN '{}' of workflow-module '{}'",
                                bpmn.getResourceName(), workflowModuleId);
                        final var model = bpmnParser.parseModelFromStream(inputStream);
        
                        processBpmnModel(workflowModuleId, deployedProcesses, bpmn, model);
                        
                    } catch (IOException e) {
                        throw new RuntimeException(e.getMessage());
                    }
                    
                });
        
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
                .flatMap(process ->
                        Stream.of(
                            taskWiring.connectablesForType(process, model, ServiceTask.class),
                            taskWiring.connectablesForType(process, model, BusinessRuleTask.class),
                            taskWiring.connectablesForType(process, model, SendTask.class),
                            taskWiring.connectablesForType(process, model, UserTask.class),
                            taskWiring.connectablesForType(process, model, IntermediateThrowEvent.class),
                            taskWiring.connectablesForType(process, model, EndEvent.class)
                        )
                        .flatMap(i -> i) // map stream of streams to one stream
                    )
                .forEach(connectable -> taskWiring.wireTask(processService[0], connectable));
    	
    }
    
}
