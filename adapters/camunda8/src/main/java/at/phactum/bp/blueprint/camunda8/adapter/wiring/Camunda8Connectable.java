package at.phactum.bp.blueprint.camunda8.adapter.wiring;

import at.phactum.bp.blueprint.bpm.deployment.Connectable;
import at.phactum.bp.blueprint.service.WorkflowTask;
import io.camunda.zeebe.model.bpmn.instance.Process;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeLoopCharacteristics;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeTaskDefinition;

public class Camunda8Connectable implements Connectable {
    
    private Process process;
    private String elementId;
    private ZeebeTaskDefinition taskDefinition;
    
    public Camunda8Connectable(
            final Process process,
            final String elementId,
            final ZeebeTaskDefinition taskDefinition,
            final ZeebeLoopCharacteristics loopCharacteristics) {

        this.process = process;
        this.elementId = elementId;
        this.taskDefinition = taskDefinition;

    }
    
    @Override
    public boolean applies(
            final WorkflowTask workflowTask) {
        
        return getElementId().equals(workflowTask.id())
                || getTaskDefinition().equals(workflowTask.taskDefinition());

    }
    
    public String getElementId() {

        return elementId;

    }
    
    @Override
    public boolean isExecutableProcess() {

        return process.isExecutable();

    }

    @Override
    public String getBpmnProcessId() {

        return process.getId();

    }
    
    @Override
    public String getTaskDefinition() {

        if (taskDefinition == null) {
            return null;
        }
        return taskDefinition.getType();

    }
    
}