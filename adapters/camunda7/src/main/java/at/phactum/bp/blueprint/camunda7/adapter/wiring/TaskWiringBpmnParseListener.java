package at.phactum.bp.blueprint.camunda7.adapter.wiring;

import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;

public class TaskWiringBpmnParseListener extends AbstractBpmnParseListener {

    private Camunda7TaskWiring taskWiring;

    public TaskWiringBpmnParseListener(Camunda7TaskWiring taskWiring) {

        super();
        this.taskWiring = taskWiring;

    }

}
