package at.phactum.bp.blueprint.process;

import at.phactum.bp.blueprint.domain.WorkflowDomainEntity;

/**
 * @param <DE> The workflow-aggregate domain-entity-class
 */
public interface ProcessService<DE extends WorkflowDomainEntity> {

    /**
     * Start a new workflow.
     * 
     * @param domainEntity The domain-entity
     * @return The domain-entity attached to JPA
     */
    DE startWorkflow(DE domainEntity) throws Exception;

    /**
     * @return The BPMN process-id this service belongs to
     */
    String getBpmnProcessId();

}
