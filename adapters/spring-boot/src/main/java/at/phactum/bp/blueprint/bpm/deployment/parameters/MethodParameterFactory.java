package at.phactum.bp.blueprint.bpm.deployment.parameters;

import at.phactum.bp.blueprint.domain.WorkflowDomainEntity;
import at.phactum.bp.blueprint.service.MultiInstanceElementResolver;

public class MethodParameterFactory {

    public DomainEntityMethodParameter getDomainEntityMethodParameter() {

        return new DomainEntityMethodParameter();

    }

    public MultiInstanceElementMethodParameter getMultiInstanceElementMethodParameter(String name) {

        return new MultiInstanceElementMethodParameter(name);

    }

    public MultiInstanceIndexMethodParameter getMultiInstanceIndexMethodParameter(String name) {

        return new MultiInstanceIndexMethodParameter(name);

    }

    public MultiInstanceTotalMethodParameter getMultiInstanceTotalMethodParameter(String name) {

        return new MultiInstanceTotalMethodParameter(name);

    }

    public ResolverBasedMultiInstanceMethodParameter getResolverBasedMultiInstanceMethodParameter(
            MultiInstanceElementResolver<? extends WorkflowDomainEntity, ?> resolverBean) {

        return new ResolverBasedMultiInstanceMethodParameter(resolverBean);

    }

}
