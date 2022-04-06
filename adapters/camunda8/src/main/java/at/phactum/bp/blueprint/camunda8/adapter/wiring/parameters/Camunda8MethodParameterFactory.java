package at.phactum.bp.blueprint.camunda8.adapter.wiring.parameters;

import at.phactum.bp.blueprint.bpm.deployment.parameters.MethodParameterFactory;
import at.phactum.bp.blueprint.bpm.deployment.parameters.MultiInstanceElementMethodParameter;
import at.phactum.bp.blueprint.bpm.deployment.parameters.MultiInstanceIndexMethodParameter;
import at.phactum.bp.blueprint.bpm.deployment.parameters.MultiInstanceTotalMethodParameter;
import at.phactum.bp.blueprint.bpm.deployment.parameters.ResolverBasedMultiInstanceMethodParameter;
import at.phactum.bp.blueprint.bpm.deployment.parameters.TaskParameter;
import at.phactum.bp.blueprint.service.MultiInstanceElementResolver;

public class Camunda8MethodParameterFactory extends MethodParameterFactory {

    @Override
    public ResolverBasedMultiInstanceMethodParameter getResolverBasedMultiInstanceMethodParameter(
            final MultiInstanceElementResolver<?, ?> resolverBean) {

        return new Camunda8ResolverBasedMethodParameter(resolverBean);

    }

    @Override
    public MultiInstanceElementMethodParameter getMultiInstanceElementMethodParameter(
            final String name) {

        return new Camunda8MultiInstanceElementMethodParameter(name);

    }

    @Override
    public MultiInstanceIndexMethodParameter getMultiInstanceIndexMethodParameter(
            final String name) {

        return new Camunda8MultiInstanceIndexMethodParameter(name);

    }

    @Override
    public MultiInstanceTotalMethodParameter getMultiInstanceTotalMethodParameter(
            final String name) {

        return new Camunda8MultiInstanceTotalMethodParameter(name);

    }
    
    @Override
    public TaskParameter getTaskParameter(
            final String name) {
        
        return new Camunda8TaskParameter(name);
        
    }
    
}
