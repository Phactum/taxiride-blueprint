package at.phactum.bp.blueprint.bpm.deployment.parameters;

import at.phactum.bp.blueprint.domain.WorkflowDomainEntity;
import at.phactum.bp.blueprint.service.MultiInstanceElementResolver;

public class ResolverBasedMultiInstanceMethodParameter extends MethodParameter {

    protected final MultiInstanceElementResolver<? extends WorkflowDomainEntity, ?> resolverBean;

    public ResolverBasedMultiInstanceMethodParameter(
            final MultiInstanceElementResolver<? extends WorkflowDomainEntity, ?> resolverBean) {
        
        this.resolverBean = resolverBean;
        
    }
    
    public MultiInstanceElementResolver<? extends WorkflowDomainEntity, ?> getResolverBean() {
        return resolverBean;
    }
    
}
