package at.phactum.bp.blueprint.camunda8.adapter.wiring;

import at.phactum.bp.blueprint.bpm.deployment.Connectable;
import io.camunda.zeebe.model.bpmn.instance.Process;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeLoopCharacteristics;

public class Camunda8Connectable implements Connectable {
    
    public static enum Type {
        TASK, USERTASK
    };

    private Process process;

    private String elementId;
    
    private Type type;

    private String taskDefinition;
    
    public Camunda8Connectable(
            final Process process,
            final String elementId,
            final Type type,
            final String taskDefinition,
            final ZeebeLoopCharacteristics loopCharacteristics) {

        this.process = process;
        this.elementId = elementId;
        this.taskDefinition = taskDefinition;
        this.type = type;

    }
    
    @Override
    public String getElementId() {

        return elementId;

    }
    
    public Type getType() {
        
        return type;
        
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

        return taskDefinition;

    }
    
}