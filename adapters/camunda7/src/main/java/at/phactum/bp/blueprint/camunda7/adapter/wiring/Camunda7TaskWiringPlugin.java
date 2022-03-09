package at.phactum.bp.blueprint.camunda7.adapter.wiring;

import java.util.ArrayList;

import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;

public class Camunda7TaskWiringPlugin extends AbstractProcessEnginePlugin {

    private TaskWiringBpmnParseListener taskWiringBpmnParseListener;

    public Camunda7TaskWiringPlugin(
            final TaskWiringBpmnParseListener taskWiringBpmnParseListener) {
        
        this.taskWiringBpmnParseListener = taskWiringBpmnParseListener;
        
    }

    @Override
    public void preInit(final ProcessEngineConfigurationImpl configuration) {

        var preParseListeners = configuration.getCustomPreBPMNParseListeners();
        if (preParseListeners == null) {
            preParseListeners = new ArrayList<>();
            configuration.setCustomPreBPMNParseListeners(preParseListeners);
        }
        preParseListeners.add(taskWiringBpmnParseListener);

    }

}
