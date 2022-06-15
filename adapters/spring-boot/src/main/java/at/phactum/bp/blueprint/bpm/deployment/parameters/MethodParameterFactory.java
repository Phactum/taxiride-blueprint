package at.phactum.bp.blueprint.bpm.deployment.parameters;

import at.phactum.bp.blueprint.service.MultiInstanceElementResolver;
import at.phactum.bp.blueprint.service.TaskEvent;

public class MethodParameterFactory {

    public DomainEntityMethodParameter getDomainEntityMethodParameter() {

        return new DomainEntityMethodParameter();

    }

    public MultiInstanceElementMethodParameter getMultiInstanceElementMethodParameter(
            final String name) {

        return new MultiInstanceElementMethodParameter(name);

    }

    public MultiInstanceIndexMethodParameter getMultiInstanceIndexMethodParameter(
            final String name) {

        return new MultiInstanceIndexMethodParameter(name);

    }

    public MultiInstanceTotalMethodParameter getMultiInstanceTotalMethodParameter(
            final String name) {

        return new MultiInstanceTotalMethodParameter(name);

    }

    public ResolverBasedMultiInstanceMethodParameter getResolverBasedMultiInstanceMethodParameter(
            final MultiInstanceElementResolver<?, ?> resolverBean) {

        return new ResolverBasedMultiInstanceMethodParameter(resolverBean);

    }
    
    public TaskParameter getTaskParameter(
            final String name) {
        
        return new TaskParameter(name);

    }
    
    public TaskIdMethodParameter getTaskIdParameter() {
        
        return new TaskIdMethodParameter();
        
    }

    public TaskEventMethodParameter getUserTaskEventParameter(
            final TaskEvent.Event[] events) {
        
        return new TaskEventMethodParameter(events);
        
    }
    
}
