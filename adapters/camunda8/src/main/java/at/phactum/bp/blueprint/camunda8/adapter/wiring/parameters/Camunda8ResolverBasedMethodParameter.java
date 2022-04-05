package at.phactum.bp.blueprint.camunda8.adapter.wiring.parameters;

import java.util.List;
import java.util.stream.Collectors;

import at.phactum.bp.blueprint.bpm.deployment.parameters.ResolverBasedMultiInstanceMethodParameter;
import at.phactum.bp.blueprint.service.MultiInstanceElementResolver;

public class Camunda8ResolverBasedMethodParameter extends ResolverBasedMultiInstanceMethodParameter
        implements ParameterVariables {

    public Camunda8ResolverBasedMethodParameter(
            final MultiInstanceElementResolver<?, ?> resolverBean) {

        super(resolverBean);

    }
    
    @Override
    public List<String> getVariables() {
        
        return resolverBean
                .getNames()
                .stream()
                .flatMap(name -> List.of(
                        name,
                        name + Camunda8MultiInstanceTotalMethodParameter.SUFFIX,
                        name + Camunda8MultiInstanceIndexMethodParameter.SUFFIX).stream())
                .collect(Collectors.toList());

    }

}
