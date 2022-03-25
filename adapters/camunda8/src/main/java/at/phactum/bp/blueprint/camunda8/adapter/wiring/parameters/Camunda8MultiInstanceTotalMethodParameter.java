package at.phactum.bp.blueprint.camunda8.adapter.wiring.parameters;

import java.util.List;

import at.phactum.bp.blueprint.bpm.deployment.parameters.MultiInstanceTotalMethodParameter;

public class Camunda8MultiInstanceTotalMethodParameter extends MultiInstanceTotalMethodParameter
        implements ParameterVariables {

    public static final String SUFFIX = "_total";

    public Camunda8MultiInstanceTotalMethodParameter(
            final String name) {
        
        super(name);
        
    }

    @Override
    public List<String> getVariables() {

        return List.of(name + SUFFIX);

    }

}
