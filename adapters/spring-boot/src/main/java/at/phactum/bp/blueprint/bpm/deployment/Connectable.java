package at.phactum.bp.blueprint.bpm.deployment;

import at.phactum.bp.blueprint.service.WorkflowTask;

public interface Connectable {

    boolean applies(WorkflowTask workflowTask);

    String getBpmnProcessId();

    boolean isExecutableProcess();

    String getTaskDefinition();

}
