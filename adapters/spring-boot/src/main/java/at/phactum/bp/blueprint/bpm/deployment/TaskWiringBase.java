package at.phactum.bp.blueprint.bpm.deployment;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;

import at.phactum.bp.blueprint.domain.WorkflowDomainEntity;
import at.phactum.bp.blueprint.process.ProcessService;
import at.phactum.bp.blueprint.service.WorkflowService;
import at.phactum.bp.blueprint.service.WorkflowServicePort;
import at.phactum.bp.blueprint.service.WorkflowTask;

public abstract class TaskWiringBase<T extends Connectable, PS extends ProcessService<?>> {

    protected final ApplicationContext applicationContext;

    public TaskWiringBase(
            final ApplicationContext applicationContext) {
        
        this.applicationContext = applicationContext;
        
    }

    protected abstract <DE extends WorkflowDomainEntity> PS connectToBpms(
            final Class<DE> workflowDomainEntityClass,
            final String bpmnProcessId);

    protected Entry<Class<?>, Class<? extends WorkflowDomainEntity>> determineWorkflowEntityClass(
            final Object bean) {

        final var serviceClass = targetClass(bean);

        if (serviceClass.isAssignableFrom(WorkflowServicePort.class)) {
            return null;
        }
        
        try {
            
            @SuppressWarnings("unchecked")
            final var workflowDomainEntityClass =
                    (Class<? extends WorkflowDomainEntity>) Arrays
                            .stream(bean
                                    .getClass()
                                    .getGenericInterfaces())
                            .map(type -> (ParameterizedType) type)
                            .filter(type -> type.getRawType().equals(WorkflowServicePort.class))
                            .findFirst()
                            .get().getActualTypeArguments()[0];

            return Map.entry(serviceClass, workflowDomainEntityClass);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Could not load WorkflowDomainEntity", e);
        }

    }

    private boolean isExtendingWorkflowServicePort(
            final Entry<Class<?>, Class<? extends WorkflowDomainEntity>> classes) {

        return classes != null;

    }

    public PS wireService(
            final String bpmnProcessId) {

        final var workflowDomainEntityClass = determineAndValidateWorkflowDomainEntityClass(bpmnProcessId);

        return connectToBpms(workflowDomainEntityClass, bpmnProcessId);
        
    }

    private Class<? extends WorkflowDomainEntity> determineAndValidateWorkflowDomainEntityClass(
            final String bpmnProcessId) {

        final var tested = new StringBuilder();
        
        final var matchingServices = applicationContext
                .getBeansWithAnnotation(WorkflowService.class)
                .entrySet()
                .stream()
                .peek(bean -> {
                    if (tested.length() > 0) {
                        tested.append(", ");
                    }
                    tested.append(bean.getKey());
                })
                .filter(bean -> isAboutConnectableProcess(bpmnProcessId, bean.getValue()))
                .map(bean -> determineWorkflowEntityClass(bean.getValue()))
                .filter(this::isExtendingWorkflowServicePort)
                .collect(Collectors.groupingBy(
                        Entry::getValue,
                        Collectors.mapping(Entry::getKey, Collectors.toList())));

        if (matchingServices.size() == 0) {
            throw new RuntimeException(
                    "No bean annotated with @WorkflowService(bpmnProcessId=\""
                    + bpmnProcessId
                    + "\") and extending "
                    + WorkflowServicePort.class.getName()
                    + " found. Tested for: "
                    + tested);
        }
        
        if (matchingServices.size() != 1) {
            
            final var found = new StringBuilder();
            matchingServices
                    .entrySet()
                    .stream()
                    .peek(entry -> {
                        if (found.length() > 0) {
                            found.append("; ");
                        }
                        found.append(entry.getKey().getName());
                        found.append(" by ");
                    })
                    .flatMap(entry -> entry.getValue().stream())
                    .forEach(matchingService -> {
                        if (found.length() > 0) {
                            found.append(", ");
                        }
                        found.append(matchingService.getName());
                    });
            throw new RuntimeException(
                    "Beans annotated with @WorkflowService(bpmnProcessId=\""
                    + bpmnProcessId
                    + "\") and extending "
                    + WorkflowServicePort.class.getName()
                    + " found having different generic parameters, but should all the same: "
                    + found);
            
        }

        return matchingServices.keySet().iterator().next();

    }

    public void wireTask(
            final PS processService,
            final T connectable) {

        applicationContext
                .getBeansWithAnnotation(WorkflowService.class)
                .entrySet()
                .stream()
                .filter(bean -> isAboutConnectableProcess(
                        connectable.getBpmnProcessId(),
                        bean.getValue()))
                .forEach(bean -> connectConnectableToBean(
                        processService,
                        connectable,
                        bean.getKey(),
                        bean.getValue()));

    }

    private boolean isAboutConnectableProcess(
            final String bpmnProcessId,
            final Object bean) {
        
        final var beanClass = targetClass(bean);
        final var workflowServiceAnnotations = beanClass.getAnnotationsByType(WorkflowService.class);

        return Arrays
                .stream(workflowServiceAnnotations)
                .anyMatch(annotation -> annotation.bpmnProcessId().equals(bpmnProcessId));

    }
    
    protected Class<?> targetClass(
            final Object bean) {
        
        return AopUtils.getTargetClass(bean);
        
    }

    protected abstract void connectToBpms(
            final PS processService,
            final Object bean,
            final T connectable,
            final Method method);

    private void connectConnectableToBean(
            final PS processService,
            final T connectable,
            final String beanName,
            final Object bean) {
        
        final Class<?> beanClass = targetClass(bean);
        
        final var tested = new StringBuilder();
        final var matching = new StringBuilder();
        final var matchingMethods = new AtomicInteger(0);
        
        Arrays
                .stream(beanClass.getMethods())
                .flatMap(method -> Arrays
                        .stream(method.getAnnotationsByType(WorkflowTask.class))
                        .map(annotation -> Map.entry(method, annotation)))
                .peek(m -> {
                    if (tested.length() > 0) {
                        tested.append(", ");
                    }
                    tested.append(beanName);
                    tested.append('#');
                    tested.append(m.getKey().toString());
                })
                .filter(m -> connectable.applies(m.getValue()))
                .peek(m -> {
                    if (matching.length() > 0) {
                        matching.append(", ");
                    }
                    matching.append(beanName);
                    matching.append('#');
                    matching.append(m.getKey().toString());
                })
                .filter(m -> matchingMethods.getAndIncrement() == 0)
                .findFirst()
                .ifPresent(m -> connectToBpms(
                        processService,
                        bean,
                        connectable,
                        m.getKey()));
        
        if (matchingMethods.get() > 1) {
            throw new RuntimeException(
                    "More than one method annotated with @WorkflowTask is matching task '"
                    + connectable.getTaskDefinition()
                    + "' of process '"
                    + connectable.getBpmnProcessId()
                    + "': "
                    + matching);
        }
        if (matchingMethods.get() == 0) {
            throw new RuntimeException(
                    "No method annotated with @WorkflowTask is matching task '"
                    + connectable.getTaskDefinition()
                    + "' of process '"
                    + connectable.getBpmnProcessId()
                    + "'. Tested for: "
                    + tested);
        }

    }
    
}
