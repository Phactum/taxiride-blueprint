package at.phactum.bp.blueprint.port;

import at.phactum.bp.blueprint.domain.WorkflowDomainEntity;

/**
 * @param <DE> The workflow-aggregate domain-entity-class
 */
public abstract class WorkflowServicePort<DE extends WorkflowDomainEntity> {

    /**
     * Start a new workflow.
     * 
     * @param domainEntity The underlying domain-entity
     */
    public abstract void startWorkflow(DE domainEntity);

}
