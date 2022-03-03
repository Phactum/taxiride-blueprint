package at.phactum.bp.blueprint.camunda8.adapter;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

import at.phactum.bp.blueprint.service.WorkflowService;
import at.phactum.bp.blueprint.service.WorkflowTask;
import io.camunda.zeebe.client.ZeebeClient;

public class Camunda8TaskWiring {
    
    @Value("${workerId}")
    private String workerId;
    
    @Autowired
    private ZeebeClient client;
    
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ObjectProvider<Camunda8TaskHandler> taskHandlers;

    public void wireTask(final Connectable connectable) {

        applicationContext
                .getBeansWithAnnotation(WorkflowService.class)
                .entrySet()
                .stream()
                .filter(bean -> isAboutConnectableProcess(connectable, bean.getKey()))
                .forEach(bean -> saveWorkflowTaskMethods(connectable, bean.getKey(), bean.getValue()));

    }

    private boolean isAboutConnectableProcess(final Connectable connectable, final String beanName) {

        final var workflowService = applicationContext.findAnnotationOnBean(beanName, WorkflowService.class);

        if (workflowService == null) {
            return false;
        }

        return workflowService.bpmnProcessId().equals(connectable.getProcess().getId());

    }

    private void saveWorkflowTaskMethods(
            final Connectable connectable,
            final String beanName,
            final Object bean) {
        
        final Class<?> beanClass = AopUtils.getTargetClass(bean);
        
        final var matching = new StringBuilder();
        
        final var matchingMethods = Arrays
                .stream(beanClass.getMethods())
                .flatMap(method -> Arrays
                        .stream(method.getAnnotationsByType(WorkflowTask.class))
                        .map(annotation -> Map.entry(method, annotation)))
                .filter(m -> connectable.getElementId().equals(m.getValue().id())
                        || connectable.getTaskDefinition().equals(m.getValue().taskDefinition()))
                .peek(m -> {
                    if (matching.length() == 0) {
                        connectToCamunda(bean, connectable, m.getKey());
                    }

                    if (matching.length() > 0) {
                        matching.append(", ");
                    }
                    matching.append(beanName);
                    matching.append('#');
                    matching.append(m.getKey().toString());
                })
                .map(m -> connectToCamunda(bean, connectable, m.getKey())).collect(Collectors.toList());
        
        if (matchingMethods.size() > 1) {
            throw new RuntimeException(
                    "More than one method annotated with @WorkflowTask is matching task '"
                    + connectable.getTaskDefinition()
                    + "' of process '"
                    + connectable.getProcess().getId()
                    + "': "
                    + matching);
        }
        if (matchingMethods.size() == 0) {
            throw new RuntimeException(
                    "No method annotated with @WorkflowTask is matching task '"
                    + connectable.getTaskDefinition()
                    + "' of process '"
                    + connectable.getProcess().getId()
                    + "': "
                    + matching);
        }

        // start worker
        matchingMethods.iterator().next().run();

    }
    
    private Runnable connectToCamunda(
            final Object bean,
            final Connectable connectable,
            final Method method) {
        
        final var taskHandler = taskHandlers.getObject(
                connectable.getTaskDefinition(),
                bean,
                method);

        return () -> client
                .newWorker()
                .jobType(connectable.getTaskDefinition())
                .handler(taskHandler)
                .name(workerId)
                .fetchVariables(List.of())
                .open();

              // using defaults from config if null, 0 or negative
//              if (zeebeWorkerValue.getName() != null && zeebeWorkerValue.getName().length() > 0) {
//                builder.name(zeebeWorkerValue.getName());
//              } else {
//                builder.name(beanInfo.getBeanName() + "#" + zeebeWorkerValue.getMethodInfo().getMethodName());
//              }
//              if (zeebeWorkerValue.getMaxJobsActive() > 0) {
//                builder.maxJobsActive(zeebeWorkerValue.getMaxJobsActive());
//              }
//              if (zeebeWorkerValue.getTimeout() > 0) {
//                builder.timeout(zeebeWorkerValue.getTimeout());
//              }
//              if (zeebeWorkerValue.getPollInterval() > 0) {
//                builder.pollInterval(Duration.ofMillis(zeebeWorkerValue.getPollInterval()));
//              }
//              if (zeebeWorkerValue.getRequestTimeout() > 0) {
//                builder.requestTimeout(Duration.ofSeconds(zeebeWorkerValue.getRequestTimeout()));
//              }
//              if (zeebeWorkerValue.getFetchVariables().length > 0) {
//                builder.fetchVariables(zeebeWorkerValue.getFetchVariables());
//              }
        
    }

}
