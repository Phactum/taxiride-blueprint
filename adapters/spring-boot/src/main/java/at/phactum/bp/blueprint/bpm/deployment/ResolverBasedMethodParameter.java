package at.phactum.bp.blueprint.bpm.deployment;

import at.phactum.bp.blueprint.domain.WorkflowDomainEntity;
import at.phactum.bp.blueprint.service.MultiInstanceElementResolver;

public class ResolverBasedMethodParameter extends MethodParameter {

    private MultiInstanceElementResolver<? extends WorkflowDomainEntity, ?> resolverBean;
    
    public ResolverBasedMethodParameter(
            final Type type,
            final MultiInstanceElementResolver<? extends WorkflowDomainEntity, ?> resolverBean) {
        
        super(type);
        this.resolverBean = resolverBean;
        
    }
    
    public MultiInstanceElementResolver<? extends WorkflowDomainEntity, ?> getResolverBean() {
        return resolverBean;
    }

}
