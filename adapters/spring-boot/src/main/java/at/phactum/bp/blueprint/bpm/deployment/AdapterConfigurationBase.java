package at.phactum.bp.blueprint.bpm.deployment;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import org.springframework.beans.factory.InjectionPoint;
import org.springframework.core.ResolvableType;
import org.springframework.data.jpa.repository.JpaRepository;

import at.phactum.bp.blueprint.utilities.SpringDataTool;

public abstract class AdapterConfigurationBase<P extends ProcessServiceImplementation<?>> {

    private Map<Class<?>, P> connectableServices = new HashMap<>();

    protected Collection<P> getConnectableServices() {

        return connectableServices.values();

    }

    @SuppressWarnings("unchecked")
    protected <DE> P registerProcessService(
            final SpringDataTool springDataTool,
            final InjectionPoint injectionPoint,
            final BiFunction<JpaRepository<DE, String>, Class<DE>, P> processServiceBeanSupplier) throws Exception {
        
        final var resolvableType = ResolvableType.forField(injectionPoint.getField());

        final var workflowDomainEntityClass = (Class<DE>) resolvableType
                .getGeneric(0)
                .resolve();

        final var existingService = connectableServices.get(workflowDomainEntityClass);
        if (existingService != null) {
            return (P) existingService;
        }

        final var workflowDomainEntityRepository = springDataTool
                .getJpaRepository(workflowDomainEntityClass);

        final var result = processServiceBeanSupplier.apply(
                workflowDomainEntityRepository,
                workflowDomainEntityClass);

        connectableServices.put(workflowDomainEntityClass, result);

        return result;
        
    }

}
