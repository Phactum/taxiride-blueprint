package at.phactum.bp.blueprint.camunda7.adapter.wiring;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.StringUtil;
import org.camunda.bpm.engine.impl.util.xml.Element;

import at.phactum.bp.blueprint.camunda7.adapter.wiring.Camunda7Connectable.Type;

public class TaskWiringBpmnParseListener extends AbstractBpmnParseListener {

    private static final Pattern CAMUNDA_EL_PATTERN = Pattern.compile("^[\\$\\#]\\{(.*)\\}$");
    
    private final Camunda7TaskWiring taskWiring;

    private List<Camunda7Connectable> connectables = new LinkedList<>();

    private Map<String, Camunda7Connectable> serviceTaskLikeElements = new HashMap<>();

    public TaskWiringBpmnParseListener(Camunda7TaskWiring taskWiring) {

        super();
        this.taskWiring = taskWiring;

    }

    @Override
    public void parseProcess(
            final Element processElement,
            final ProcessDefinitionEntity processDefinition) {

        final var bpmnProcessId = processDefinition.getKey();

        final var processService = taskWiring.wireService(bpmnProcessId);

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
        
        connectEvent(intermediateEventElement, scope, activity);
        
    }
    
    @Override
    public void parseBusinessRuleTask(
            final Element businessRuleTaskElement,
            final ScopeImpl scope,
            final ActivityImpl activity) {
        
        connect(businessRuleTaskElement, scope, activity);
        
    }
    
    @Override
    public void parseSendTask(
            final Element sendTaskElement,
            final ScopeImpl scope,
            final ActivityImpl activity) {
        
        connect(sendTaskElement, scope, activity);
        
    }

    @Override
    public void parseServiceTask(
            final Element serviceTaskElement,
            final ScopeImpl scope,
            final ActivityImpl activity) {
        
        connect(serviceTaskElement, scope, activity);
        
    }

    private void connect(
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

    private void connectEvent(final Element eventElement, final ScopeImpl scope, final ActivityImpl activity) {

        final var messageEventDefinition = eventElement.element(BpmnParse.MESSAGE_EVENT_DEFINITION);
        if (messageEventDefinition == null) {
            return;
        }

        final var id = messageEventDefinition.attribute("id");
        final var connectable = serviceTaskLikeElements.get(id);
        if (connectable == null) {
            return;
        }

        connectables.add(new Camunda7Connectable(connectable.getBpmnProcessId(), activity.getId(),
                connectable.getTaskDefinition(), connectable.getType()));

    }

    private String unwrapExpression(final ActivityImpl activity, final String delegateExpression) {
        
        final var expressionWrapperMatcher = CAMUNDA_EL_PATTERN.matcher(delegateExpression);
        
        if (!expressionWrapperMatcher.find()) {
            throw new RuntimeException(
                    "'delegate-expression' of element '"
                    + activity.getId()
                    + "' not uses pattern ${...} or #{...}!");
        }
        
        return expressionWrapperMatcher.group(1);
        
    }

}
