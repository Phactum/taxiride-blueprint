package at.phactum.bp.blueprint.bpm.deployment;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;

import at.phactum.bp.blueprint.bpm.deployment.parameters.MethodParameter;
import at.phactum.bp.blueprint.bpm.deployment.parameters.MethodParameterFactory;
import at.phactum.bp.blueprint.service.BpmnProcess;
import at.phactum.bp.blueprint.service.MultiInstanceElement;
import at.phactum.bp.blueprint.service.MultiInstanceIndex;
import at.phactum.bp.blueprint.service.MultiInstanceTotal;
import at.phactum.bp.blueprint.service.NoResolver;
import at.phactum.bp.blueprint.service.TaskParam;
import at.phactum.bp.blueprint.service.WorkflowService;
import at.phactum.bp.blueprint.service.WorkflowTask;

public abstract class TaskWiringBase<T extends Connectable, PS extends ProcessServiceImplementation<?>> {

    protected final ApplicationContext applicationContext;
    
    protected final MethodParameterFactory methodParameterFactory;

    public TaskWiringBase(
            final ApplicationContext applicationContext,
            final MethodParameterFactory methodParameterFactory) {
        
        this.applicationContext = applicationContext;
        this.methodParameterFactory = methodParameterFactory;
        
    }

    public TaskWiringBase(
            final ApplicationContext applicationContext) {
        
        this(applicationContext, new MethodParameterFactory());
        
    }

    protected abstract <DE> PS connectToBpms(
            String workflowModuleId, Class<DE> workflowDomainEntityClass, String bpmnProcessId);

    protected Entry<Class<?>, Class<?>> determineWorkflowEntityClass(
            final Object bean) {

        final var serviceClass = targetClass(bean);

        final var aggregateClassNames = new LinkedList<String>();
        
        final var workflowDomainEntityClass = Arrays
                .stream(serviceClass.getAnnotationsByType(WorkflowService.class))
                .collect(Collectors.groupingBy(annotation -> annotation.workflowAggregateClass()))
                .keySet()
                .stream()
                .peek(aggregateClass -> aggregateClassNames.add(aggregateClass.getName()))
                .findFirst()
                .get();
        
        return Map.entry(
                serviceClass,
                workflowDomainEntityClass);

    }

    private boolean isExtendingWorkflowServicePort(
            final Entry<Class<?>, Class<?>> classes) {

        return classes != null;

    }

    public PS wireService(
            final String workflowModuleId,
            final String bpmnProcessId) {

        final var workflowDomainEntityClass = determineAndValidateWorkflowDomainEntityClass(bpmnProcessId);

        return connectToBpms(workflowModuleId, workflowDomainEntityClass, bpmnProcessId);
        
    }

    private Class<?> determineAndValidateWorkflowDomainEntityClass(
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
                    + "\"). Tested for: "
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
                    "Multiple beans annotated with @WorkflowService(bpmnProcessId=\""
                    + bpmnProcessId
                    + "\") found having different generic parameters, but should all the same: "
                    + found);
            
        }

        return matchingServices.keySet().iterator().next();

    }

    public void wireTask(
            final PS processService,
            final T connectable) {

        final var tested = new StringBuilder();
        final var matching = new StringBuilder();
        final var matchingMethods = new AtomicInteger(0);

        applicationContext
                .getBeansWithAnnotation(WorkflowService.class)
                .entrySet()
                .stream()
                .filter(bean -> isAboutConnectableProcess(
                        connectable.getBpmnProcessId(),
                        bean.getValue()))
                .forEach(bean -> {
                    connectConnectableToBean(
                        processService,
                        connectable,
                            tested, matching, matchingMethods,
                        bean.getKey(),
                            bean.getValue());
                });

        if (matchingMethods.get() > 1) {
            throw new RuntimeException("More than one method annotated with @WorkflowTask is matching task '"
                    + connectable.getTaskDefinition() + "' of process '" + connectable.getBpmnProcessId() + "': "
                    + matching);
        }
        if (matchingMethods.get() == 0) {
            throw new RuntimeException(
                    "No public method annotated with @WorkflowTask is matching task '" + connectable.getTaskDefinition()
                            + "' of process '" + connectable.getBpmnProcessId() + "'. Tested for: " + tested);
        }

    }

    private boolean isAboutConnectableProcess(
            final String bpmnProcessId,
            final Object bean) {
        
        final var beanClass = targetClass(bean);
        final var workflowServiceAnnotations = beanClass.getAnnotationsByType(WorkflowService.class);

        return Arrays
                .stream(workflowServiceAnnotations)
                .flatMap(workflowServiceAnnotation -> Arrays.stream(workflowServiceAnnotation.bpmnProcess()))
                .anyMatch(annotation -> annotation.bpmnProcessId().equals(bpmnProcessId)
                        || (annotation.bpmnProcessId().equals(BpmnProcess.USE_BEAN_NAME)
                                && bpmnProcessId.equals(beanClass.getSimpleName())));

    }
    
    protected Class<?> targetClass(
            final Object bean) {
        
        return AopUtils.getTargetClass(bean);
        
    }

    protected abstract void connectToBpms(
            PS processService, Object bean, T connectable, Method method, List<MethodParameter> parameters);

    private void connectConnectableToBean(
            final PS processService,
            final T connectable,
            final StringBuilder tested, final StringBuilder matching, final AtomicInteger matchingMethods,
            final String beanName,
            final Object bean) {
        
        final Class<?> beanClass = targetClass(bean);
        
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
                .filter(m -> connectable.getElementId().equals(m.getValue().id())
                        || connectable.getTaskDefinition().equals(m.getValue().taskDefinition())
                        || connectable.getTaskDefinition().equals(m.getKey().getName()))
                .peek(m -> {
                    if (matching.length() > 0) {
                        matching.append(", ");
                    }
                    matching.append(beanName);
                    matching.append('#');
                    matching.append(m.getKey().toString());
                })
                .filter(m -> matchingMethods.getAndIncrement() == 0)
                .map(m -> validateParameters(processService, m.getKey()))
                .forEach(m -> connectToBpms(
                        processService,
                        bean,
                        connectable,
                        m.getKey(),
                        m.getValue()));
        
    }
    
    private Map.Entry<Method, List<MethodParameter>> validateParameters(
            final PS processService,
            final Method method) {
        
        final var parameters = new LinkedList<MethodParameter>();
        
        final var workflowDomainEntityClass = processService.getWorkflowDomainEntityClass();

        final var unknown = new StringBuffer();

        if (!void.class.equals(method.getReturnType())) {
            throw new RuntimeException(
                    "Expected return-type 'void' for '"
                    + method
                    + "' but got: "
                    + method.getReturnType());
        }
        
        final var index = new int[] { -1 };
        Arrays
                .stream(method.getParameters())
                .peek(param -> ++index[0])
                .filter(param -> {
                    final var isWorkflowDomainEntity = workflowDomainEntityClass.isAssignableFrom(param.getType());
                    if (!isWorkflowDomainEntity) {
                        return true;
                    }

                    parameters.add(methodParameterFactory
                            .getDomainEntityMethodParameter());
                    return false;
                }).filter(param -> {
                    final var taskParamAnnotation = param.getAnnotation(TaskParam.class);
                    if (taskParamAnnotation == null) {
                        return true;
                    }

                    parameters.add(methodParameterFactory
                            .getTaskParameter(taskParamAnnotation.value()));
                    return false;
                }).filter(param -> {
                    final var miTotalAnnotation = param.getAnnotation(MultiInstanceTotal.class);
                    if (miTotalAnnotation == null) {
                        return true;
                    }

                    parameters.add(methodParameterFactory
                            .getMultiInstanceTotalMethodParameter(miTotalAnnotation.value()));
                    return false;
                }).filter(param -> {
                    final var miIndexAnnotation = param.getAnnotation(MultiInstanceIndex.class);
                    if (miIndexAnnotation == null) {
                        return true;
                    }

                    parameters.add(methodParameterFactory
                            .getMultiInstanceIndexMethodParameter(miIndexAnnotation.value()));
                    return false;
                }).filter(param -> {
                    final var miElementAnnotation = param.getAnnotation(MultiInstanceElement.class);
                    if (miElementAnnotation == null) {
                        return true;
                    }

                    if (!miElementAnnotation.resolverBean().equals(NoResolver.class)) {

                        final var resolver = applicationContext
                                .getBean(miElementAnnotation.resolverBean());

                        parameters.add(methodParameterFactory
                                .getResolverBasedMultiInstanceMethodParameter(resolver));

                    } else if (!MultiInstanceElement.USE_RESOLVER.equals(miElementAnnotation.value())) {

                        parameters.add(methodParameterFactory
                                .getMultiInstanceElementMethodParameter(miElementAnnotation.value()));
                        
                    } else {
                        
                        throw new RuntimeException(
                                "Either attribute 'value' or 'resolver' of annotation @"
                                + MultiInstanceElement.class.getSimpleName()
                                + " has to be defined. Missing both at parameter "
                                + index[0]
                                + " of method "
                                + method);
                        
                    }
                    return false;
                }).forEach(param -> {
                    if (unknown.length() != 0) {
                        unknown.append(", ");
                    }
                    unknown.append(index[0]);
                    unknown.append(" (");
                    unknown.append(param.getType());
                    unknown.append(' ');
                    unknown.append(param.getName());
                    unknown.append(")");
                });

        if (unknown.length() != 0) {
            throw new RuntimeException(
                    "Unexpected parameter(s) in method '"
                    + method.getName()
                            + "': "
                    + unknown);
        }
        
        return Map.entry(method, parameters);

    }

}
