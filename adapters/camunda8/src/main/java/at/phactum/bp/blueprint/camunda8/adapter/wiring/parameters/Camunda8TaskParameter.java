package at.phactum.bp.blueprint.camunda8.adapter.wiring.parameters;

import java.util.List;

import at.phactum.bp.blueprint.bpm.deployment.parameters.TaskParameter;

public class Camunda8TaskParameter extends TaskParameter implements ParameterVariables {

    public Camunda8TaskParameter(
            final String name) {
        
        super(name);

    }
    
    @Override
    public List<String> getVariables() {
        
        return List.of(getName());
        
    }

}
