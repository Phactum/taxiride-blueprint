package at.phactum.bp.blueprint.camunda7.adapter.wiring;

import java.beans.FeatureDescriptor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.javax.el.ELContext;
import org.camunda.bpm.engine.impl.javax.el.ELResolver;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.springframework.context.ApplicationContext;

import at.phactum.bp.blueprint.camunda7.adapter.wiring.Camunda7Connectable.Type;

/*
 * Custom expression language resolver to resolve process entities
 * by using correspondingly named spring data repositories.
 */
public class ProcessEntityELResolver extends ELResolver {

    private final ApplicationContext applicationContext;
    
    private final Map<Camunda7Connectable, Camunda7TaskHandler> taskHandlers = new HashMap<>();

    public ProcessEntityELResolver(
            final ApplicationContext applicationContext) {
        
        this.applicationContext = applicationContext;
        
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
        
        final var handler = taskHandlers
                .entrySet()
                .stream()
                .filter(entry -> {
                    final var connectable = entry.getKey();
                    
                    if (!connectable.getBpmnProcessId().equals(bpmnProcessId)) {
                        return false;
                    }
                    
                    final var element = execution.getBpmnModelElementInstance();
                    
                    if ((connectable.getElementId() != null)
                            && element.getId().equals(connectable.getElementId())) {
                        return true;
                    }
                    
                    return false;
                })
                .findFirst()
                .get();
        
        return executeHandler(execution, handler.getKey(), handler.getValue());

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
