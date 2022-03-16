package at.phactum.bp.blueprint.bpm.deployment;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.data.jpa.repository.JpaRepository;

import at.phactum.bp.blueprint.bpm.deployment.MethodParameter.Type;
import at.phactum.bp.blueprint.domain.WorkflowDomainEntity;
import at.phactum.bp.blueprint.service.MultiInstanceElementResolver;

public class TaskHandlerBase {

    protected final JpaRepository<WorkflowDomainEntity, String> workflowDomainEntityRepository;

    protected final List<MethodParameter> parameters;

    protected final Object bean;

    protected final Method method;

    public TaskHandlerBase(
            final JpaRepository<WorkflowDomainEntity, String> workflowDomainEntityRepository,
            final Object bean,
            final Method method,
            final List<MethodParameter> parameters) {
        
        this.workflowDomainEntityRepository = workflowDomainEntityRepository;
        this.bean = bean;
        this.method = method;
        this.parameters = parameters;

    }
    
    @SuppressWarnings("unchecked")
    protected Object execute(
            final String workflowDomainEntityId,
            final Supplier<Map<String, MultiInstanceElementResolver.MultiInstance<Object>>> multiInstanceSupplier)
            throws Exception {
        
        final var domainEntity = new WorkflowDomainEntity[] { null };
        final var multiInstance = new Map[] { null };

        final var args = new Object[parameters.size()];
        final var index = new int[] { -1 };
        parameters
                .stream()
                .peek(param -> ++index[0])
                .filter(param -> processDomainEntityParameter(
                        args, index[0], param, domainEntity, workflowDomainEntityId))
                .filter(param -> processMultiInstanceTotalParameter(args, index[0], param,
                        () -> cachedMultiInstance(multiInstanceSupplier, multiInstance)))
                .filter(param -> processMultiInstanceIndexParameter(args, index[0], param,
                        () -> cachedMultiInstance(multiInstanceSupplier, multiInstance)))
                .filter(param -> processMultiInstanceElementParameter(args, index[0], param,
                        () -> cachedMultiInstance(multiInstanceSupplier, multiInstance)))
                .filter(param -> processMultiInstanceResolverParameter(args, index[0], param,
                        () -> cachedMultiInstance(multiInstanceSupplier, multiInstance)))
                // ignore unknown parameters, but they should be filtered as part of validation
                .forEach(param -> { /* */ });
        
        final var result = method.invoke(bean, args);

        if (domainEntity[0] != null) {
            workflowDomainEntityRepository.saveAndFlush(domainEntity[0]);
        }

        return result;
        
    }
    
    private Map<String, MultiInstanceElementResolver.MultiInstance<Object>> cachedMultiInstance(
            final Supplier<Map<String, MultiInstanceElementResolver.MultiInstance<Object>>> multiInstanceSupplier,
            final Map<String, MultiInstanceElementResolver.MultiInstance<Object>>[] cache) {

        if (cache[0] == null) {
            cache[0] = multiInstanceSupplier.get();
        }
        
        return cache[0];

    }

    private boolean processMultiInstanceTotalParameter(
            final Object[] args,
            final int index,
            final MethodParameter param,
            final Supplier<Map<String, MultiInstanceElementResolver.MultiInstance<Object>>> multiInstanceSupplier) {

        if (param.getType() != Type.MULTIINSTANCE_TOTAL) {
            return true;
        }

        return processMultiInstanceParameter(args, index, param, multiInstanceSupplier,
                MultiInstanceElementResolver.MultiInstance::getTotal);
        
    }

    private boolean processMultiInstanceIndexParameter(
            final Object[] args,
            final int index,
            final MethodParameter param,
            final Supplier<Map<String, MultiInstanceElementResolver.MultiInstance<Object>>> multiInstanceSupplier) {

        if (param.getType() != Type.MULTIINSTANCE_INDEX) {
            return true;
        }

        return processMultiInstanceParameter(args, index, param, multiInstanceSupplier,
                MultiInstanceElementResolver.MultiInstance::getIndex);
        
    }

    private boolean processMultiInstanceElementParameter(
            final Object[] args,
            final int index,
            final MethodParameter param,
            final Supplier<Map<String, MultiInstanceElementResolver.MultiInstance<Object>>> multiInstanceSupplier) {

        if (param.getType() != Type.MULTIINSTANCE_ELEMENT) {
            return true;
        }
        
        return processMultiInstanceParameter(args, index, param, multiInstanceSupplier,
                MultiInstanceElementResolver.MultiInstance::getElement);
        
    }

    private boolean processMultiInstanceParameter(
            final Object[] args,
            final int index,
            final MethodParameter param,
            final Supplier<Map<String, MultiInstanceElementResolver.MultiInstance<Object>>> multiInstanceSupplier,
            final Function<MultiInstanceElementResolver.MultiInstance<Object>, Object> resultFunction) {
        
        final var multiInstance = multiInstanceSupplier.get();
        
        if ((multiInstance == null)
                || multiInstance.isEmpty()) {
            throw new RuntimeException("No multi-instance context available!");
        }

        final var multiInstanceActivityId = new String[] { null };
        multiInstance
                .keySet()
                .forEach(activityId -> multiInstanceActivityId[0] = activityId);
        args[index] = resultFunction.apply(multiInstance.get(multiInstanceActivityId[0]));

        return false;

    }
    
    private boolean processMultiInstanceResolverParameter(
            final Object[] args,
            final int index,
            final MethodParameter param,
            final Supplier<Map<String, MultiInstanceElementResolver.MultiInstance<Object>>> multiInstanceSupplier) {

        if (param.getType() != Type.MULTIINSTANCE_RESOLVER) {
            return true;
        }

        final var multiInstances = multiInstanceSupplier.get();
        
        final var resolver = ((ResolverBasedMethodParameter) param).getResolverBean();
        args[index] = resolver.resolve(null, multiInstances);
        
        return false;
        
    }
    
    private boolean processDomainEntityParameter(
            final Object[] args,
            final int index,
            final MethodParameter param,
            final WorkflowDomainEntity[] domainEntity,
            final String workflowDomainEntityId) {

        if (param.getType() != Type.DOMAINENTITY) {
            return true;
        }
        
        domainEntity[0] = (WorkflowDomainEntity) workflowDomainEntityRepository
                .getById(workflowDomainEntityId);
        args[index] = domainEntity[0];

        return false;
        
    }
    
}
