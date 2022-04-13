package at.phactum.bp.blueprint.bpm.deployment;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.core.ResolvableType;
import org.springframework.data.jpa.repository.JpaRepository;

import at.phactum.bp.blueprint.utilities.SpringDataTool;

public abstract class AdapterConfigurationBase<P extends ProcessServiceImplementation<?>> {

    private Map<Class<?>, P> connectableServices = new HashMap<>();

    protected abstract <DE> P buildProcessServiceBean(
            final JpaRepository<DE, String> workflowDomainEntityRepository,
            final Class<DE> workflowDomainEntityClass,
            final Function<DE, String> getDomainEntityId);

    protected Collection<P> getConnectableServices() {

        return connectableServices.values();

    }

    @SuppressWarnings("unchecked")
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public <DE> P camunda7ProcessService(
            final SpringDataTool springDataTool,
            final InjectionPoint injectionPoint) throws Exception {

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

        final var result = buildProcessServiceBean(
                workflowDomainEntityRepository,
                workflowDomainEntityClass,
                domainEntity -> springDataTool.getDomainEntityId(domainEntity));

        connectableServices.put(workflowDomainEntityClass, result);

        return result;

    }
    
}
