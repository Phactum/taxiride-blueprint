package at.phactum.bp.blueprint.camunda7.adapter.wiring;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.listener.DelegateExpressionExecutionListener;
import org.camunda.bpm.engine.impl.bpmn.listener.ExpressionExecutionListener;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.camunda.bpm.engine.impl.util.StringUtil;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.springframework.util.StringUtils;

import at.phactum.bp.blueprint.camunda7.adapter.wiring.Camunda7Connectable.Type;

public class TaskWiringBpmnParseListener extends AbstractBpmnParseListener {

    private static final Pattern CAMUNDA_EL_PATTERN = Pattern.compile("^[\\$\\#]\\{(.*)\\}$");

    private static final ThreadLocal<String> workflowModuleId = new ThreadLocal<>();
    
    private final Camunda7TaskWiring taskWiring;

    private final Camunda7UserTaskEventHandler userTaskEventHandler;
    
    private List<Camunda7Connectable> connectables = new LinkedList<>();

    private Map<String, Camunda7Connectable> serviceTaskLikeElements = new HashMap<>();
    
    public TaskWiringBpmnParseListener(
            final Camunda7TaskWiring taskWiring,
            final Camunda7UserTaskEventHandler userTaskEventHandler) {

        super();
        this.taskWiring = taskWiring;
        this.userTaskEventHandler = userTaskEventHandler;

    }
    
    public static void setWorkflowModuleId(
            final String workflowModuleId) {
        
        TaskWiringBpmnParseListener.workflowModuleId.set(workflowModuleId);
        
    }
    
    public static void clearWorkflowModuleId() {
        
        TaskWiringBpmnParseListener.workflowModuleId.remove();
        
    }
    
    @Override
    public void parseProcess(
            final Element processElement,
            final ProcessDefinitionEntity processDefinition) {

        final var workflowModuleId = TaskWiringBpmnParseListener.workflowModuleId.get();
        final var bpmnProcessId = processDefinition.getKey();

        final var processService = taskWiring.wireService(workflowModuleId, bpmnProcessId);

        connectables.forEach(connectable -> taskWiring.wireTask(processService, connectable));

        connectables.clear();
        serviceTaskLikeElements.clear();

    }
    
    @Override
    public void parseEndEvent(
            final Element endEventElement,
            final ScopeImpl scope,
            final ActivityImpl activity) {
        
        connectEvent(endEventElement, scope, activity);
        
    }
    
    @Override
    public void parseIntermediateThrowEvent(
            final Element intermediateEventElement,
            final ScopeImpl scope,
            final ActivityImpl activity) {
        
        final var connected = connectEvent(intermediateEventElement, scope, activity);
        if (connected) {
            return;
        }

        final var unsupportedListeners = activity
                .getListeners()
                .values()
                .stream()
                .flatMap(List::stream)
                .filter(l -> {
                    if (l instanceof ExpressionExecutionListener) {
                        final var expression = unwrapExpression(
                                activity,
                                ((ExpressionExecutionListener) l).getExpressionText());
                        connectListener(
                                intermediateEventElement,
                                scope,
                                activity,
                                Type.EXPRESSION,
                                expression);
                        return false;
                    }
                    return true;
                })
                .filter(l -> {
                    if (l instanceof DelegateExpressionExecutionListener) {
                        final var expression = unwrapExpression(
                                activity,
                                ((DelegateExpressionExecutionListener) l).getExpressionText());
                        connectListener(
                                intermediateEventElement,
                                scope,
                                activity,
                                Type.DELEGATE_EXPRESSION,
                                expression);
                        return false;
                    }
                    return true;
                })
                .map(l -> l.toString())
                .collect(Collectors.joining(", "));
        
        if (StringUtils.hasText(unsupportedListeners)) {
            throw new RuntimeException(
                    "Unsupported listeners at '"
                    + activity.getId()
                    + "': "
                    + unsupportedListeners);
        }
        
    }
    
    @Override
    public void parseUserTask(
            final Element userTaskElement,
            final ScopeImpl scope,
            final ActivityImpl activity) {
        
        final var taskDefinition = getTaskDefinition(activity);

        taskDefinition.addBuiltInTaskListener(
                org.camunda.bpm.engine.delegate.TaskListener.EVENTNAME_CREATE,
                userTaskEventHandler);
//        taskDefinition.addBuiltInTaskListener(
//                org.camunda.bpm.engine.delegate.TaskListener.EVENTNAME_DELETE,
//                null);

        final var bpmnProcessId = ((ProcessDefinitionEntity) activity.getProcessDefinition()).getKey();

        final var connectable = new Camunda7Connectable(
                bpmnProcessId,
                activity.getId(),
                taskDefinition.getFormKey() != null ? taskDefinition.getFormKey().getExpressionText() : null,
                Type.USERTASK);
        
        connectables.add(connectable);

    }

    private void connectListener(
            final Element element,
            final ScopeImpl scope,
            final ActivityImpl activity,
            final Type type,
            final String expression) {
        
        final var bpmnProcessId = ((ProcessDefinitionEntity) activity.getProcessDefinition()).getKey();
        
        final var connectable = new Camunda7Connectable(
                bpmnProcessId,
                activity.getId(),
                expression,
                type);
        
        connectables.add(connectable);
        
    }
    
    @Override
    public void parseBusinessRuleTask(
            final Element businessRuleTaskElement,
            final ScopeImpl scope,
            final ActivityImpl activity) {
        
        connectTask(businessRuleTaskElement, scope, activity);
        
    }
    
    @Override
    public void parseSendTask(
            final Element sendTaskElement,
            final ScopeImpl scope,
            final ActivityImpl activity) {
        
        connectTask(sendTaskElement, scope, activity);
        
    }

    @Override
    public void parseServiceTask(
            final Element serviceTaskElement,
            final ScopeImpl scope,
            final ActivityImpl activity) {
        
        connectTask(serviceTaskElement, scope, activity);
        
    }

    private void connectTask(
            final Element element,
            final ScopeImpl scope,
            final ActivityImpl activity) {
        
        final var bpmnProcessId = ((ProcessDefinitionEntity) activity.getProcessDefinition()).getKey();

        final var delegateExpression = element.attributeNS(
                BpmnParse.CAMUNDA_BPMN_EXTENSIONS_NS,
                BpmnParse.PROPERTYNAME_DELEGATE_EXPRESSION);

        final var expression = element.attributeNS(
                BpmnParse.CAMUNDA_BPMN_EXTENSIONS_NS,
                BpmnParse.PROPERTYNAME_EXPRESSION);
        
        final var topic = element.attributeNS(
                BpmnParse.CAMUNDA_BPMN_EXTENSIONS_NS,
                BpmnParse.PROPERTYNAME_EXTERNAL_TASK_TOPIC);
        
        final Camunda7Connectable connectable;
        if (StringUtil.hasText(delegateExpression)) {
            
            final var unwrappedDelegateExpression = unwrapExpression(activity, delegateExpression);
            
            connectable = new Camunda7Connectable(
                    bpmnProcessId,
                    activity.getId(),
                    unwrappedDelegateExpression,
                    Type.DELEGATE_EXPRESSION);
            
        } else if (StringUtil.hasText(expression)) {
            
            final var unwrappedExpression = unwrapExpression(activity, expression);
            
            connectable = new Camunda7Connectable(
                    bpmnProcessId,
                    activity.getId(),
                    unwrappedExpression,
                    Type.EXPRESSION);
            
        } else if (StringUtil.hasText(topic)) {
            
            connectable = new Camunda7Connectable(
                    bpmnProcessId,
                    activity.getId(),
                    topic,
                    Type.EXTERNAL_TASK);
            
        } else {
            
            throw new RuntimeException(
                    "Missing implemenation 'delegate-expression' or 'external-task topic' on element '"
                    + activity.getId()
                    + "'");
                    
        }
        
        if (element.getTagName().equals("messageEventDefinition")) {
            serviceTaskLikeElements.put(activity.getId(), connectable);
        } else {
            connectables.add(connectable);
        }
        
    }

    private boolean connectEvent(final Element eventElement, final ScopeImpl scope, final ActivityImpl activity) {

        final var messageEventDefinition = eventElement.element(BpmnParse.MESSAGE_EVENT_DEFINITION);
        if (messageEventDefinition == null) {
            return false;
        }

        final var id = messageEventDefinition.attribute("id");
        final var connectable = serviceTaskLikeElements.get(id);
        if (connectable == null) {
            return false;
        }

        connectables.add(
                new Camunda7Connectable(
                        connectable.getBpmnProcessId(),
                        activity.getId(),
                        connectable.getTaskDefinition(),
                        connectable.getType()));

        return true;

    }

    private String unwrapExpression(final ActivityImpl activity, final String delegateExpression) {
        
        final var expressionWrapperMatcher = CAMUNDA_EL_PATTERN.matcher(delegateExpression);
        
        if (!expressionWrapperMatcher.find()) {
            throw new RuntimeException(
                    "'delegate-expression' of element '"
                    + activity.getId()
                    + "' not uses pattern ${...} or #{...}: '"
                    + delegateExpression
                    + "'");
        }
        
        return expressionWrapperMatcher.group(1);
        
    }

    /**
     * Retrieves task definition.
     *
     * @param activity the taskActivity
     * @return taskDefinition for activity
     */
    private TaskDefinition getTaskDefinition(
            final ActivityImpl activity) {
        
        final UserTaskActivityBehavior activityBehavior = (UserTaskActivityBehavior) activity.getActivityBehavior();
        return activityBehavior.getTaskDefinition();
        
    }
    
}
