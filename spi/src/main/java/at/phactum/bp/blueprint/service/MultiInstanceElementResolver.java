package at.phactum.bp.blueprint.service;

import java.util.Collection;
import java.util.Map;

import at.phactum.bp.blueprint.domain.WorkflowDomainEntity;

public interface MultiInstanceElementResolver<DE extends WorkflowDomainEntity, T> {

    interface MultiInstance<E> {
        E getElement();

        int getIndex();

        int getTotal();
    };

    /**
     * @return The name of variables/fields which hold the current value of the
     *         multi-instance iterations
     */
    Collection<String> getNames();

    /**
     * Determines an object passed as a {@link WorkflowTask} annotated method's
     * parameter annotated by {@link MultiInstanceElement} having an attribute
     * {@link MultiInstanceElement#resolverBean()} set.
     * 
     * @param domainEntity   The current workflow's domain-entity
     * @param multiInstances a sorted map of all context-information for all active
     *                       multi-instance executions. Key is the name of the
     *                       multi-instance element. The order is from most-out to
     *                       most-inner execution.
     * @return A value which will be passed as a parameter
     */
    T resolve(DE domainEntity, Map<String, MultiInstance<Object>> multiInstances);

}
