package at.phactum.bp.blueprint.bpm.deployment.parameters;

import at.phactum.bp.blueprint.service.MultiInstanceElementResolver;

public class ResolverBasedMultiInstanceMethodParameter extends MethodParameter {

    protected final MultiInstanceElementResolver<?, ?> resolverBean;

    public ResolverBasedMultiInstanceMethodParameter(
            final MultiInstanceElementResolver<?, ?> resolverBean) {
        
        this.resolverBean = resolverBean;
        
    }
    
    public MultiInstanceElementResolver<?, ?> getResolverBean() {
        return resolverBean;
    }
    
}
