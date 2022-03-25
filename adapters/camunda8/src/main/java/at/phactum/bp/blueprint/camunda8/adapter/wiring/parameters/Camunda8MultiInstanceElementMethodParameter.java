package at.phactum.bp.blueprint.camunda8.adapter.wiring.parameters;

import java.util.List;

import at.phactum.bp.blueprint.bpm.deployment.parameters.MultiInstanceElementMethodParameter;

public class Camunda8MultiInstanceElementMethodParameter extends MultiInstanceElementMethodParameter
        implements ParameterVariables {

    public Camunda8MultiInstanceElementMethodParameter(
            final String name) {
        
        super(name);
        
    }

    @Override
    public List<String> getVariables() {

        return List.of(name);

    }

}
