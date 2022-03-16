package at.phactum.bp.blueprint.camunda7.adapter.wiring;

import at.phactum.bp.blueprint.bpm.deployment.Connectable;
import at.phactum.bp.blueprint.service.WorkflowTask;

public class Camunda7Connectable implements Connectable {

    public static enum Type {
        EXPRESSION, DELEGATE_EXPRESSION, EXTERNAL_TASK
    };
    
    private final Type type;
    private final String bpmnProcessId;
    private final String elementId;
    private final String taskDefinition;
    
    public Camunda7Connectable(
            final String bpmnProcessId,
            final String elementId,
            final String taskDefinition,
            final Type type) {

        this.bpmnProcessId = bpmnProcessId;
        this.elementId = elementId;
        this.taskDefinition = taskDefinition;
        this.type = type;

    }
    
    @Override
    public boolean applies(
            final WorkflowTask workflowTask) {

        return applies(workflowTask.id(), workflowTask.taskDefinition());

    }
    
    public boolean applies(
            final String elementId,
            final String taskDefinition) {
        
        return getElementId().equals(elementId)
                || getTaskDefinition().equals(taskDefinition);
        
    }
    
    @Override
    public boolean isExecutableProcess() {
        
        return true;
        
    }
    
    public Type getType() {

        return type;

    }
    
    public String getElementId() {
        
        return elementId;
        
    }
    
    @Override
    public String getBpmnProcessId() {
        
        return bpmnProcessId;
        
    }

    @Override
    public String getTaskDefinition() {
        
        return taskDefinition;
        
    }
    
}