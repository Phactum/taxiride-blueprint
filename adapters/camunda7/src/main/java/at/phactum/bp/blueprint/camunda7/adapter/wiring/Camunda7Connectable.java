package at.phactum.bp.blueprint.camunda7.adapter.wiring;

import org.camunda.bpm.model.bpmn.instance.Process;

import at.phactum.bp.blueprint.bpm.deployment.Connectable;
import at.phactum.bp.blueprint.service.WorkflowTask;

public class Camunda7Connectable implements Connectable {
    
    private Process process;
    private String elementId;
    private String taskDefinition;
    
    public Camunda7Connectable(
            final Process process,
            final String elementId,
            final String taskDefinition) {
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
    
    public boolean isExecutableProcess() {
        
        return process.isExecutable();
        
    }
    
    public String getElementId() {
        
        return elementId;
        
    }
    
    @Override
    public String getBpmnProcessId() {
        
        return process.getId();
        
    }

    public String getTaskDefinition() {
        
        return taskDefinition;
        
    }
    
}