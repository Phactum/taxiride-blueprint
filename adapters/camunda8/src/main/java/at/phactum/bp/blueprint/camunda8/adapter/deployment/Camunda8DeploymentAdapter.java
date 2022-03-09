package at.phactum.bp.blueprint.camunda8.adapter.deployment;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.google.common.collect.Streams;

import at.phactum.bp.blueprint.bpm.deployment.ModuleAwareBpmnDeployment;
import at.phactum.bp.blueprint.camunda8.adapter.wiring.Camunda8TaskWiring;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.model.bpmn.impl.BpmnModelInstanceImpl;
import io.camunda.zeebe.model.bpmn.impl.BpmnParser;
import io.camunda.zeebe.model.bpmn.instance.BusinessRuleTask;
import io.camunda.zeebe.model.bpmn.instance.EndEvent;
import io.camunda.zeebe.model.bpmn.instance.IntermediateThrowEvent;
import io.camunda.zeebe.model.bpmn.instance.Process;
import io.camunda.zeebe.model.bpmn.instance.SendTask;
import io.camunda.zeebe.model.bpmn.instance.ServiceTask;
import io.camunda.zeebe.spring.client.ZeebeClientLifecycle;

public class Camunda8DeploymentAdapter extends ModuleAwareBpmnDeployment
        implements Consumer<ZeebeClient> {

	private static final Logger logger = LoggerFactory.getLogger(Camunda8DeploymentAdapter.class);
	
	private final BpmnParser bpmnParser = new BpmnParser();
	
    private ZeebeClient client;

    private Camunda8TaskWiring taskWiring;

    public Camunda8DeploymentAdapter(
            final ZeebeClientLifecycle clientLifecycle,
            final Camunda8TaskWiring taskWiring) {
        
        super();
        this.taskWiring = taskWiring;
        
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

    }

    @Override
    protected void doDeployment(
    		final String workflowModuleId,
            final Resource[] bpmns,
            final Resource[] dmns,
            final Resource[] cmms) throws Exception {

        final var deployProcessCommand = client.newDeployCommand();

        Arrays
                .stream(bpmns)
                .map(resource -> {
                    try (InputStream inputStream = resource.getInputStream()) {
                        logger.info("About to deploy '{}' of workflow-module '{}'",
                                resource.getFilename(), workflowModuleId);
                    	final var model = bpmnParser.parseModelFromStream(inputStream);
                    	processBpmnModel(model);
                    	return deployProcessCommand.addProcessModel(model, resource.getFilename());
                    } catch (IOException e) {
                        throw new RuntimeException(e.getMessage());
                    }
                })
                .filter(Objects::nonNull)
                .reduce((first, second) -> second)
                .map(command -> command.send().join())
                .orElseThrow();

    }
    
    private void processBpmnModel(
    		final BpmnModelInstanceImpl model) {

        taskWiring.accept(client);

        model.getModelElementsByType(Process.class)
                .stream()
                .filter(Process::isExecutable)
                // wire service port
                .peek(process -> taskWiring.wireService(process.getId()))
                // wire task methods
                .flatMap(process -> Streams.concat(
                        taskWiring.connectablesForType(process, model, ServiceTask.class),
                        taskWiring.connectablesForType(process, model, BusinessRuleTask.class),
                        taskWiring.connectablesForType(process, model, SendTask.class),
                        taskWiring.connectablesForType(process, model, IntermediateThrowEvent.class),
                        taskWiring.connectablesForType(process, model, EndEvent.class)))
                .forEach(taskWiring::wireTask);
    	
    }

}
