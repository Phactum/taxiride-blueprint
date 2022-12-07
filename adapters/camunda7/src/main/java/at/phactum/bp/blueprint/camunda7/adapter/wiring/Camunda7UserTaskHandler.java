package at.phactum.bp.blueprint.camunda7.adapter.wiring;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;

import at.phactum.bp.blueprint.bpm.deployment.MultiInstance;
import at.phactum.bp.blueprint.bpm.deployment.TaskHandlerBase;
import at.phactum.bp.blueprint.bpm.deployment.parameters.MethodParameter;
import at.phactum.bp.blueprint.bpm.deployment.parameters.TaskEventMethodParameter;
import at.phactum.bp.blueprint.service.TaskEvent;
import at.phactum.bp.blueprint.service.TaskEvent.Event;

public class Camunda7UserTaskHandler extends TaskHandlerBase implements TaskListener {

    private static final Logger logger = LoggerFactory.getLogger(Camunda7UserTaskHandler.class);

    public Camunda7UserTaskHandler(
            final JpaRepository<Object, String> workflowDomainEntityRepository,
            final Object bean,
            final Method method,
            final List<MethodParameter> parameters) {
        
        super(workflowDomainEntityRepository, bean, method, parameters);
        
    }

    @Override
    protected Logger getLogger() {

        return logger;

    }

    @Override
    public void notify(final DelegateTask delegateTask) {
        
        final var multiInstanceCache = new Map[] { null };

        try {

            final var execution = delegateTask.getExecution();

            super.execute(
                    execution.getBusinessKey(),
                    multiInstanceActivity -> {
                        if (multiInstanceCache[0] == null) {
                            multiInstanceCache[0] = Camunda7TaskHandler.getMultiInstanceContext(execution);
                        }
                        return multiInstanceCache[0].get(multiInstanceActivity);
                    },
                    taskParameter -> execution.getVariableLocal(taskParameter),
                    () -> delegateTask.getId(),
                    () -> getTaskEvent(delegateTask.getEventName()));

        } catch (RuntimeException e) {

            throw e;

        } catch (Exception e) {

            throw new RuntimeException(e);

        }

    }
    
    public boolean eventApplies(
            final String eventName) {
        
        final var event = getTaskEvent(eventName);
        if (event == null) {
            return false;
        }
        
        return this.parameters
                .stream()
                .filter(parameter -> parameter instanceof TaskEventMethodParameter)
                .map(parameter -> ((TaskEventMethodParameter) parameter).getEvents())
                .findFirst()
                .orElse(Set.of())
                .contains(event);
        
    }
    
    protected TaskEvent.Event getTaskEvent(
            final String eventName) {
        
        switch (eventName) {
        case org.camunda.bpm.engine.delegate.TaskListener.EVENTNAME_DELETE:
            return Event.CANCELED;
        case org.camunda.bpm.engine.delegate.TaskListener.EVENTNAME_CREATE:
            return Event.CREATED;
        default:
            return null;
        }
        
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected MultiInstance<Object> getMultiInstance(
            final String name,
            final Function<String, Object> multiInstanceSupplier) {
        
        return (MultiInstance<Object>) multiInstanceSupplier.apply(name);
        
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected Object getMultiInstanceElement(
            final String name,
            final Function<String, Object> multiInstanceSupplier) {
        
        return ((MultiInstance<Object>) multiInstanceSupplier.apply(name)).getElement();
        
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Integer getMultiInstanceTotal(
            final String name,
            final Function<String, Object> multiInstanceSupplier) {
        
        return ((MultiInstance<Object>) multiInstanceSupplier.apply(name)).getTotal();
        
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Integer getMultiInstanceIndex(
            final String name,
            final Function<String, Object> multiInstanceSupplier) {
        
        return ((MultiInstance<Object>) multiInstanceSupplier.apply(name)).getIndex();
        
    }

}
