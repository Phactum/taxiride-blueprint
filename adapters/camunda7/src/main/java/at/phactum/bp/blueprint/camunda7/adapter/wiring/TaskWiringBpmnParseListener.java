package at.phactum.bp.blueprint.camunda7.adapter.wiring;

import java.util.LinkedList;
import java.util.List;
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

    }

    @Override
    public void parseServiceTask(
            final Element serviceTaskElement,
            final ScopeImpl scope,
            final ActivityImpl activity) {
        
        final var bpmnProcessId = ((ProcessDefinitionEntity) activity.getProcessDefinition()).getKey();

        final var delegateExpression = serviceTaskElement.attributeNS(
                BpmnParse.CAMUNDA_BPMN_EXTENSIONS_NS,
                BpmnParse.PROPERTYNAME_DELEGATE_EXPRESSION);

        final var expression = serviceTaskElement.attributeNS(
                BpmnParse.CAMUNDA_BPMN_EXTENSIONS_NS,
                BpmnParse.PROPERTYNAME_EXPRESSION);
        
        final var topic = serviceTaskElement.attributeNS(
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
                    "Missing implemenation 'delegate-expression' or 'external-task topic' on service-task '"
                    + activity.getId()
                    + "'");
                    
        }
        
        connectables.add(connectable);
        
    }

    private String unwrapExpression(final ActivityImpl activity, final String delegateExpression) {
        
        final var expressionWrapperMatcher = CAMUNDA_EL_PATTERN.matcher(delegateExpression);
        
        if (!expressionWrapperMatcher.find()) {
            throw new RuntimeException(
                    "'delegate-expression' of service-task '"
                    + activity.getId()
                    + "' not uses pattern ${...} or #{...}!");
        }
        
        return expressionWrapperMatcher.group(1);
        
    }
}
