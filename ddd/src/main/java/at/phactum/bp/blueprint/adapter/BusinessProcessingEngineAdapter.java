package at.phactum.bp.blueprint.adapter;

import at.phactum.bp.blueprint.domain.WorkflowDomainEntity;

/**
 * @param <DE> The workflow-aggregate domain-entity-class
 */
public interface BusinessProcessingEngineAdapter<DE extends WorkflowDomainEntity> {

    /**
     * Start a new workflow.
     * 
     * @param domainEntity The underlying domain-entity
     */
    void startWorkflow(DE domainEntity);

}
