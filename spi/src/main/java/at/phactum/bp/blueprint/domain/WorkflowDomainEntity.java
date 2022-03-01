package at.phactum.bp.blueprint.domain;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * Base-contract for business-processing based domain-driven entities.
 * <p>
 * Each workflow has it's own entity-record which is a domain-context used by
 * expressions (e.g. timer-events, conditional-flows, etc.).
 * <p>
 * Hint: Workflows spawned by call-activities may use the parent-workflow's
 * entity or use a separate entity-type if a also a separate workflow-service is
 * provided.
 */
@MappedSuperclass
public abstract class WorkflowDomainEntity {

    /**
     * The unique workflow-id used as an identifier for the underlying workflow.
     */
    @Column(name = "WORKFLOW_ID")
    private String workflowId;

    /**
     * The unique workflow-id of a root-workflow (super-parent) or null if there is
     * no parent-workflow.
     */
    @Column(name = "WORKFLOW_ID")
    private String rootWorkflowId;

    /**
     * The process-id used to start the underlying workflow.
     */
    @Column(name = "PROCESS_ID")
    private String processId;

    /**
     * @return The deployed-process-id used to start the underlying workflow
     */
    @Column(name = "PROCESS_ID_DEPLOYED")
    private String deployedProcessId;

    @Id
    public String getId() {
        return workflowId;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getRootWorkflowId() {
        return rootWorkflowId;
    }

    public void setRootWorkflowId(String rootWorkflowId) {
        this.rootWorkflowId = rootWorkflowId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getDeployedProcessId() {
        return deployedProcessId;
    }

    public void setDeployedProcessId(String deployedProcessId) {
        this.deployedProcessId = deployedProcessId;
    }

}
