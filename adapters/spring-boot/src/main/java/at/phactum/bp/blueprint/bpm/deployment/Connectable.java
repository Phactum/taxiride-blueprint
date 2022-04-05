package at.phactum.bp.blueprint.bpm.deployment;

public interface Connectable {

    String getBpmnProcessId();

    boolean isExecutableProcess();

    String getTaskDefinition();
    
    String getElementId();

}
