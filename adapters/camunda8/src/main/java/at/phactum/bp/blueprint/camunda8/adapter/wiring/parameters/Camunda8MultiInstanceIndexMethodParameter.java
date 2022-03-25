package at.phactum.bp.blueprint.camunda8.adapter.wiring.parameters;

import java.util.List;

import at.phactum.bp.blueprint.bpm.deployment.parameters.MultiInstanceIndexMethodParameter;

public class Camunda8MultiInstanceIndexMethodParameter extends MultiInstanceIndexMethodParameter
        implements ParameterVariables {

    public static final String SUFFIX = "_index";

    public Camunda8MultiInstanceIndexMethodParameter(
            final String name) {

        super(name);

    }

    @Override
    public List<String> getVariables() {
        
        return List.of(name + SUFFIX);
        
    }

}
