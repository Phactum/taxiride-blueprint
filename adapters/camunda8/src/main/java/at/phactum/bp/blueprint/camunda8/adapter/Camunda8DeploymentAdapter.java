package at.phactum.bp.blueprint.camunda8.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import com.google.common.collect.Streams;

import at.phactum.bp.blueprint.bpm.deployment.ModuleAwareBpmnDeployment;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.model.bpmn.impl.BpmnModelInstanceImpl;
import io.camunda.zeebe.model.bpmn.impl.BpmnParser;
import io.camunda.zeebe.model.bpmn.instance.BaseElement;
import io.camunda.zeebe.model.bpmn.instance.BusinessRuleTask;
import io.camunda.zeebe.model.bpmn.instance.EndEvent;
import io.camunda.zeebe.model.bpmn.instance.IntermediateThrowEvent;
import io.camunda.zeebe.model.bpmn.instance.Process;
import io.camunda.zeebe.model.bpmn.instance.SendTask;
import io.camunda.zeebe.model.bpmn.instance.ServiceTask;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeTaskDefinition;

public class Camunda8DeploymentAdapter extends ModuleAwareBpmnDeployment
        implements Consumer<ZeebeClient> {

	private static final Logger logger = LoggerFactory.getLogger(Camunda8DeploymentAdapter.class);
	
	private final BpmnParser bpmnParser = new BpmnParser();
	
    private ZeebeClient client;

    @Autowired
    private Camunda8TaskWiring taskWiring;

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
    	
    	Streams.concat(
                        connectablesForType(model, ServiceTask.class),
                        connectablesForType(model, BusinessRuleTask.class),
                        connectablesForType(model, SendTask.class),
                        connectablesForType(model, IntermediateThrowEvent.class),
                        connectablesForType(model, EndEvent.class)
                )
                .forEach(taskWiring::wireTask);
    	
    }
    
    private Stream<Connectable> connectablesForType(
            final BpmnModelInstanceImpl model,
            final Class<? extends BaseElement> type) {
        
        return model
                .getModelElementsByType(type)
                .stream()
                .map(element -> new Connectable(getOwningProcess(element), element.getId(),
                        element.getSingleExtensionElement(ZeebeTaskDefinition.class)))
                .filter(connectable -> connectable.getProcess().isExecutable())
                .filter(connectable -> connectable.getTaskDefinition() != null);
        
    }
    
    private Process getOwningProcess(final ModelElementInstance element) {

        if (element instanceof Process) {
            return (Process) element;
        }

        final var parent = element.getParentElement();
        if (parent == null) {
            return null;
        }

        return getOwningProcess(parent);

    }

}
