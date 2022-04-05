package at.phactum.bp.blueprint.camunda8.adapter.test.testcase;

import at.phactum.bp.blueprint.service.BpmnProcess;
import at.phactum.bp.blueprint.service.WorkflowService;
import at.phactum.bp.blueprint.service.WorkflowTask;

@WorkflowService(
        workflowAggregateClass = TestWorkflowDomainEntity.class,
        bpmnProcess = @BpmnProcess(bpmnProcessId = "Process_ConnectableTest1")
    )
public class TestWorkflowService1 {

    @WorkflowTask(taskDefinition = "SERVICE")
    public void doServiceTask() {

    }

    @WorkflowTask(taskDefinition = "BUSINESS_RULE")
    public void doBusinessRuleTask() {

    }

    @WorkflowTask(taskDefinition = "SEND")
    public void doSendTask() {

    }

    @WorkflowTask(taskDefinition = "SEND_EMBEDDED")
    public void doEmbeddedSendTask() {

    }

    @WorkflowTask(taskDefinition = "INTERMEDIATE")
    public void doIntermediateSendEvent() {

    }

    @WorkflowTask(taskDefinition = "INTERMEDIATE_EVENTBASED")
    public void doEventBasedIntermediateSendEvent() {

    }

    @WorkflowTask(taskDefinition = "END_EVENTBASED")
    public void doEventBasedSendEndEvent() {

    }

    @WorkflowTask(taskDefinition = "END")
    public void doSendEndEvent() {

    }

}
