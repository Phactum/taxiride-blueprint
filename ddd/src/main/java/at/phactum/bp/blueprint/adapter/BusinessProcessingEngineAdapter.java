package at.phactum.bp.blueprint.adapter;

import at.phactum.bp.blueprint.domain.WorkflowDomainEntity;

public interface BusinessProcessingEngineAdapter {

    /**
     * Start a new workflow.
     * 
     * @param domainEntity The underlying domain-entity
     */
    <DE extends WorkflowDomainEntity> void startWorkflow(DE domainEntity);

    /**
     * Used to load the domain-entity.
     * 
     * @param id The entity's id
     * @return The domain-entity
     */
    <DE extends WorkflowDomainEntity> DE loadDomainEntityById(String id);

    /**
     * Used to save the domain entity.
     * 
     * @param domainEntity The domain-entity
     */
    <DE extends WorkflowDomainEntity> void saveDomainEntity(DE domainEntity);

}
