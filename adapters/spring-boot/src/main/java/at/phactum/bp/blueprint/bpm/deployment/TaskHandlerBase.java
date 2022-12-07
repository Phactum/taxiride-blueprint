package at.phactum.bp.blueprint.bpm.deployment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.springframework.data.jpa.repository.JpaRepository;

import at.phactum.bp.blueprint.bpm.deployment.parameters.DomainEntityMethodParameter;
import at.phactum.bp.blueprint.bpm.deployment.parameters.MethodParameter;
import at.phactum.bp.blueprint.bpm.deployment.parameters.MultiInstanceElementMethodParameter;
import at.phactum.bp.blueprint.bpm.deployment.parameters.MultiInstanceIndexMethodParameter;
import at.phactum.bp.blueprint.bpm.deployment.parameters.MultiInstanceTotalMethodParameter;
import at.phactum.bp.blueprint.bpm.deployment.parameters.ResolverBasedMultiInstanceMethodParameter;
import at.phactum.bp.blueprint.bpm.deployment.parameters.TaskEventMethodParameter;
import at.phactum.bp.blueprint.bpm.deployment.parameters.TaskIdMethodParameter;
import at.phactum.bp.blueprint.bpm.deployment.parameters.TaskParameter;
import at.phactum.bp.blueprint.service.MultiInstanceElementResolver;
import at.phactum.bp.blueprint.service.TaskEvent;

public abstract class TaskHandlerBase {

    protected final JpaRepository<Object, String> workflowDomainEntityRepository;

    protected final List<MethodParameter> parameters;

    protected final Object bean;

    protected final Method method;

    protected abstract Logger getLogger();

    public TaskHandlerBase(
            final JpaRepository<Object, String> workflowDomainEntityRepository,
            final Object bean,
            final Method method,
            final List<MethodParameter> parameters) {
        
        this.workflowDomainEntityRepository = workflowDomainEntityRepository;
        this.bean = bean;
        this.method = method;
        this.parameters = parameters;


    }
    
    protected Object execute(
            final String workflowDomainEntityId,
            final Function<String, Object> multiInstanceSupplier,
            final Function<String, Object> taskParameterSupplier,
            final Supplier<String> userTaskIdSupplier,
            final Supplier<TaskEvent.Event> taskEventSupplier)
            throws Exception {
        
        final var domainEntity = new Object[] { null };

        final var args = new Object[parameters.size()];
        
        // first, find domain entity as a parameter if required
        final var index = new int[] { -1 };
        parameters
                .stream()
                .peek(param -> ++index[0])
                .anyMatch(param -> processDomainEntityParameter(
                        args, index[0], param, domainEntity, workflowDomainEntityId));
        
        // second, fill all the other parameters
        index[0] = -1;
        parameters
                .stream()
                .peek(param -> ++index[0])
                .filter(param -> processTaskParameter(args, index[0], param,
                        taskParameterSupplier))
                .filter(param -> processUserTaskIdParameter(args, index[0], param,
                        userTaskIdSupplier))
                .filter(param -> processTaskEventParameter(args, index[0], param, taskEventSupplier))
                .filter(param -> processMultiInstanceTotalParameter(args, index[0], param,
                        multiInstanceSupplier))
                .filter(param -> processMultiInstanceIndexParameter(args, index[0], param,
                        multiInstanceSupplier))
                .filter(param -> processMultiInstanceElementParameter(args, index[0], param,
                        multiInstanceSupplier))
                .filter(param -> processMultiInstanceResolverParameter(method, args, index[0], param,
                        () -> {
                            if (domainEntity[0] == null) {
                                domainEntity[0] = workflowDomainEntityRepository
                                        .getReferenceById(workflowDomainEntityId);
                            }
                            return domainEntity[0];
                        }, multiInstanceSupplier))
                // ignore unknown parameters, but they should be filtered as part of validation
                .forEach(param -> { /* */ });
        
        try {

            method.invoke(bean, args);

        } catch (InvocationTargetException e) {

            final var targetException = e.getTargetException();
            if (targetException instanceof Exception) {
                throw (Exception) targetException;
            } else {
                throw new RuntimeException(e);
            }

        }

        if (domainEntity[0] == null) {
            return null;
        }

        return workflowDomainEntityRepository.saveAndFlush(domainEntity[0]);
        
    }
    
    private boolean processMultiInstanceTotalParameter(
            final Object[] args,
            final int index,
            final MethodParameter param,
            final Function<String, Object> multiInstanceSupplier) {

        if (!(param instanceof MultiInstanceTotalMethodParameter)) {
            return true;
        }

        args[index] = getMultiInstanceTotal(
                ((MultiInstanceTotalMethodParameter) param).getName(),
                multiInstanceSupplier);
        
        return false;
        
    }

    private boolean processTaskEventParameter(
            final Object[] args,
            final int index,
            final MethodParameter param,
            final Supplier<TaskEvent.Event> taskEventSupplier) {

        if (!(param instanceof TaskEventMethodParameter)) {
            return true;
        }

        args[index] = taskEventSupplier.get();
        
        return false;
        
    }

    private boolean processUserTaskIdParameter(
            final Object[] args,
            final int index,
            final MethodParameter param,
            final Supplier<String> userTaskIdSupplier) {

        if (!(param instanceof TaskIdMethodParameter)) {
            return true;
        }

        args[index] = userTaskIdSupplier.get();
        
        return false;
        
    }

    private boolean processMultiInstanceIndexParameter(
            final Object[] args,
            final int index,
            final MethodParameter param,
            final Function<String, Object> multiInstanceSupplier) {

        if (!(param instanceof MultiInstanceIndexMethodParameter)) {
            return true;
        }

        args[index] = getMultiInstanceIndex(
                ((MultiInstanceIndexMethodParameter) param).getName(),
                multiInstanceSupplier);
        
        return false;
        
    }

    private boolean processMultiInstanceElementParameter(
            final Object[] args,
            final int index,
            final MethodParameter param,
            final Function<String, Object> multiInstanceSupplier) {

        if (!(param instanceof MultiInstanceElementMethodParameter)) {
            return true;
        }
        
        args[index] = getMultiInstanceElement(
                ((MultiInstanceElementMethodParameter) param).getName(),
                multiInstanceSupplier);
        
        return false;
        
    }
    
    @SuppressWarnings("unchecked")
    protected MultiInstance<Object> getMultiInstance(final String name,
            final Function<String, Object> multiInstanceSupplier) {

        return (MultiInstance<Object>) multiInstanceSupplier.apply(name);

    }

    @SuppressWarnings("unchecked")
    protected Object getMultiInstanceElement(final String name, final Function<String, Object> multiInstanceSupplier) {

        return ((MultiInstance<Object>) multiInstanceSupplier.apply(name)).getElement();

    }

    @SuppressWarnings("unchecked")
    protected Integer getMultiInstanceTotal(final String name, final Function<String, Object> multiInstanceSupplier) {

        return ((MultiInstance<Object>) multiInstanceSupplier.apply(name)).getTotal();

    }

    @SuppressWarnings("unchecked")
    protected Integer getMultiInstanceIndex(
            final String name,
            final Function<String, Object> multiInstanceSupplier) {

        return ((MultiInstance<Object>) multiInstanceSupplier.apply(name)).getIndex();

    }

    private boolean processMultiInstanceResolverParameter(
            final Method method,
            final Object[] args,
            final int index,
            final MethodParameter param,
            final Supplier<Object> workflowDomainEntity,
            final Function<String, Object> multiInstanceSupplier) {

        if (!(param instanceof ResolverBasedMultiInstanceMethodParameter)) {
            return true;
        }
        
        @SuppressWarnings("unchecked")
        final var resolver =
                (MultiInstanceElementResolver<Object, Object>)
                ((ResolverBasedMultiInstanceMethodParameter) param).getResolverBean();

        final var multiInstances = new HashMap<String, MultiInstanceElementResolver.MultiInstance<Object>>();
        
        resolver
                .getNames()
                .forEach(name -> multiInstances.put(name, getMultiInstance(name, multiInstanceSupplier)));

        try {
            args[index] = resolver.resolve(workflowDomainEntity.get(), multiInstances);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed processing MultiInstanceElementResolver for parameter '"
                    + index
                    + "' of method '"
                    + method
                    + "'", e);
        }
        
        return false;
        
    }
    
    private boolean processTaskParameter(
            final Object[] args,
            final int index,
            final MethodParameter param,
            final Function<String, Object> taskParameterSupplier) {
        
        if (!(param instanceof TaskParameter)) {
            return true;
        }
        
        args[index] = taskParameterSupplier.apply(
                ((TaskParameter) param).getName());
        
        return false;
        
    }
            
    
    private boolean processDomainEntityParameter(
            final Object[] args,
            final int index,
            final MethodParameter param,
            final Object[] domainEntity,
            final String workflowDomainEntityId) {

        if (!(param instanceof DomainEntityMethodParameter)) {
            return true;
        }
        
        // Using findById is required to get an object instead of a Hibernate proxy.
        // Otherwise for e.g. Camunda8 connector JSON serialization of the
        // domain-entity is not possible.
        domainEntity[0] = workflowDomainEntityRepository
                .findById(workflowDomainEntityId)
                .get();

        args[index] = domainEntity[0];

        return false;
        
    }
    
}
