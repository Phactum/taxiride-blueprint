package at.phactum.bp.blueprint.service;

import at.phactum.bp.blueprint.adapter.BusinessProcessingEngineAdapter;
import at.phactum.bp.blueprint.domain.WorkflowDomainEntity;

/**
 * A service used to support processing workflows of a certain BPMN process.
 * This interface is not used directly, but will be
 * <p>
 * Use {@link WorkflowService} to annotate classes implementing this interface.
 * At least one annotation is required!
 * <p>
 * Use {@link WorkflowTask} to annotate methods used for processing certain
 * process-tasks (e.g. service-task, send-task, etc.).
 * 
 * @param <DE> The workflow's entity
 */
public interface WorkflowDomainEntityAdapter<DE extends WorkflowDomainEntity> {

    BusinessProcessingEngineAdapter getBusinessProcessingEngineAdapter();

    /**
     * Start a new workflow.
     * 
     * @param domainEntity The underlying domain-entity
     */
    default void startWorkflow(DE domainEntity) {
        getBusinessProcessingEngineAdapter().startWorkflow(domainEntity);
    }

    /**
     * Used to load the domain-entity.
     * 
     * @param id The entity's id
     * @return The domain-entity
     */
    default DE loadDomainEntityById(String id) {
        return getBusinessProcessingEngineAdapter().loadDomainEntityById(id);
    }

    /**
     * Used to save the domain entity.
     * 
     * @param domainEntity The domain-entity
     */
    default void saveDomainEntity(DE domainEntity) {
        getBusinessProcessingEngineAdapter().saveDomainEntity(domainEntity);
    }

}
