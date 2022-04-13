package at.phactum.bp.blueprint.camunda7.adapter.wiring;

import java.beans.FeatureDescriptor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.javax.el.ELContext;
import org.camunda.bpm.engine.impl.javax.el.ELResolver;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.phactum.bp.blueprint.camunda7.adapter.service.Camunda7ProcessService;
import at.phactum.bp.blueprint.camunda7.adapter.wiring.Camunda7Connectable.Type;
import at.phactum.bp.blueprint.utilities.CaseUtils;

/*
 * Custom expression language resolver to resolve process entities
 * by using correspondingly named spring data repositories.
 */
public class ProcessEntityELResolver extends ELResolver {
    
    private static final Logger logger = LoggerFactory.getLogger(ProcessEntityELResolver.class);
    
    private final Map<Camunda7Connectable, Camunda7TaskHandler> taskHandlers = new HashMap<>();

    private final Map<String, Camunda7ProcessService<?>> processServices;

    public ProcessEntityELResolver(
            final Collection<Camunda7ProcessService<?>> connectableServices) {

        super();
        processServices = connectableServices
                .stream()
                .collect(Collectors.toMap(Camunda7ProcessService::getBpmnProcessId, s -> s));

    }

    public void addTaskHandler(
            final Camunda7Connectable connectable,
            final Camunda7TaskHandler taskHandler) {
        
        taskHandlers.put(connectable, taskHandler);
        
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        return Object.class;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
        return null;
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) {
        return Object.class;
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) {

        // if this is a lookup for attributes then use subsequent EL-resolvers
        if (base != null) {
            return null;
        }

        final var execution = (ExecutionEntity) context
                .getELResolver()
                .getValue(context, null, "execution");
        
        final var bpmnProcessId = execution
                .getProcessDefinition()
                .getKey();
        
        final var result = taskHandlers
                .entrySet()
                .stream()
                .filter(entry -> {
                    final var connectable = entry.getKey();
                    
                    if (!connectable.getBpmnProcessId().equals(bpmnProcessId)) {
                        return false;
                    }
                    
                    final var element = execution.getBpmnModelElementInstance();
                    if (element == null) {
                        return false;
                    }
                    
                    return connectable.applies(
                            element.getId(),
                            property.toString());
                })
                .findFirst()
                // found handler-reference
                .map(handler -> executeHandler(execution, handler.getKey(), handler.getValue()))
                // otherwise it will be a domain-entity property reference
                .orElseGet(() -> {
                    final var processService = processServices.get(bpmnProcessId);
                    if (processService == null) {
                        return null;
                    }

                    final var domainEntityOptional = processService
                            .getWorkflowDomainEntityRepository()
                            .findById(execution.getBusinessKey());
                    if (domainEntityOptional.isEmpty()) {
                        return null;
                    }
                    final var domainEntity = domainEntityOptional.get();
                    
                    final var workflowDomainEntityClass = processService
                            .getWorkflowDomainEntityClass();
                    
                    // use getter
                    final var getterName = "get"
                                + CaseUtils.firstCharacterToUpperCase(property.toString());
                    try {
                        return workflowDomainEntityClass
                                .getMethod(getterName)
                                .invoke(domainEntity);
                    } catch (NoSuchMethodException e) {
                        /* ignored */
                    } catch (Exception e) {
                        logger.warn("Could not access '{}#{}'",
                                workflowDomainEntityClass.getName(), getterName, e);
                        return null;
                    }

                    // use getter for booleans
                    final var isGetterName = "is"
                            + CaseUtils.firstCharacterToUpperCase(property.toString());
                    try {
                        return workflowDomainEntityClass
                                .getMethod(isGetterName)
                                .invoke(domainEntity);
                    } catch (NoSuchMethodException e) {
                        /* ignored */
                    } catch (Exception e) {
                        logger.warn("Could not access '{}#{}'",
                                workflowDomainEntityClass.getName(), isGetterName, e);
                        return null;
                    }
                    
                    // use property
                    try {
                        final var field = workflowDomainEntityClass
                                .getDeclaredField(property.toString());
                        field.setAccessible(true);
                        return field.get(domainEntity);
                    } catch (NoSuchFieldException e) {
                        /* ignored */
                    } catch (Exception e) {
                        logger.warn("Could not access property '{}' in class '{}'",
                                property.toString(), workflowDomainEntityClass.getName(), e);
                        return null;
                    }
                    
                    return null;
                });
        
        return result;

    }
    
    private Object executeHandler(
            final ExecutionEntity execution,
            final Camunda7Connectable connectable,
            final Camunda7TaskHandler taskHandler) {
        
        if (connectable.getType() == Type.EXPRESSION) {
            
            try {
                taskHandler.execute(execution);
                return taskHandler.getResult();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("Could not execute handler", e);
            }
            
        } else if (connectable.getType() == Type.DELEGATE_EXPRESSION) {
            
            return taskHandler;
            
        }
        
        return null;
        
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {
        return true;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) {

        if (base == null && getValue(context, null, property) != null) {
            throw new ProcessEngineException("Cannot set value of '" + property +
                "', it resolves to a process entity bound to the process instance.");
        }

    }

}
