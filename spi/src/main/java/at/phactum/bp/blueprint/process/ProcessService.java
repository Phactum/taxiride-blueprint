package at.phactum.bp.blueprint.process;

/**
 * @param <DE> The workflow-aggregate domain-entity-class
 */
public interface ProcessService<DE> {

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

    /**
     * Correlate a message for the domain-entity's workflow or it's sub-workflows
     * (call-activities).
     *
     * @param processEntity The domain-entity
     * @param messageName   The message name to be correlated
     */
    void correlateMessage(DE domainEntity, String messageName);

    /**
     * Correlate a message for the domain-entity's workflow or it's sub-workflows
     * (call-activities).
     *
     * @param processEntity The domain-entity
     * @param messageName   The message name to be correlated
     * @param correlationId The correlation-id
     */
    void correlateMessage(DE domainEntity, String messageName, String correlationId);

}
