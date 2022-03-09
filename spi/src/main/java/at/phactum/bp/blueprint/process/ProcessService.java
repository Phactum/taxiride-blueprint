package at.phactum.bp.blueprint.process;

import at.phactum.bp.blueprint.domain.WorkflowDomainEntity;

/**
 * @param <DE> The workflow-aggregate domain-entity-class
 */
public interface ProcessService<DE extends WorkflowDomainEntity> {

    /**
     * Start a new workflow.
     * 
     * @param domainEntity The underlying domain-entity
     */
    String startWorkflow(DE domainEntity) throws Exception;

}
