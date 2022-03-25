package at.phactum.bp.blueprint.bpm.deployment.parameters;

public abstract class NameBasedMethodParameter extends MethodParameter {

    protected final String name;
    
    public NameBasedMethodParameter(
            final String name) {
        
        this.name = name;
        
    }
    
    public String getName() {
        
        return name;
        
    }
    
}
