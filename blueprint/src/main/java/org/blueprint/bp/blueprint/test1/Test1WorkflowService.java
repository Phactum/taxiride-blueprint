package org.blueprint.bp.blueprint.test1;

import org.springframework.stereotype.Service;

import at.phactum.bp.blueprint.service.WorkflowService;
import at.phactum.bp.blueprint.service.WorkflowServicePort;
import at.phactum.bp.blueprint.service.WorkflowTask;

@Service
@WorkflowService(bpmnProcessId = "Process_Test1")
public class Test1WorkflowService implements WorkflowServicePort<Test1DomainEntity> {

    @WorkflowTask(taskDefinition = "TEST1")
    @WorkflowTask(taskDefinition = "TEST2")
    public void doTest1Task() {
        System.err.println("JUHU");
    }

}
