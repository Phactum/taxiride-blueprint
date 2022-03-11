package at.phactum.bp.blueprint.camunda8.adapter.test.testcase;

import at.phactum.bp.blueprint.domain.WorkflowDomainEntity;

public class TestWorkflowDomainEntity extends WorkflowDomainEntity {

    @Override
    public String getWorkflowModuleId() {

        return TestModuleProperties.WORKFLOW_MODULE_ID;

    }

}
