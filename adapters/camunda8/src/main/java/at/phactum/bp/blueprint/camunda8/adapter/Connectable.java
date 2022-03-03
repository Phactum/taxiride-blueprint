package at.phactum.bp.blueprint.camunda8.adapter;

import io.camunda.zeebe.model.bpmn.instance.Process;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeTaskDefinition;

public class Connectable {
    
    private Process process;
    private String elementId;
    private ZeebeTaskDefinition taskDefinition;
    
    public Connectable(
            final Process process,
            final String elementId,
            final ZeebeTaskDefinition taskDefinition) {
        this.process = process;
        this.elementId = elementId;
        this.taskDefinition = taskDefinition;
    }
    
    public String getElementId() {
        return elementId;
    }
    
    public Process getProcess() {
        return process;
    }
    
    public String getTaskDefinition() {
        if (taskDefinition == null) {
            return null;
        }
        return taskDefinition.getType();
    }
    
}