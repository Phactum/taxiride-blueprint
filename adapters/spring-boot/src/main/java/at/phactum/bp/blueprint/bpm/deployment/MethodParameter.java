package at.phactum.bp.blueprint.bpm.deployment;

public class MethodParameter {

    public static enum Type {
        UNKNOWN,
        DOMAINENTITY,
        MULTIINSTANCE
    };
    
    private Type type;

    public MethodParameter(
            final Type type) {

        this.type = type;
        
    }
    
    public Type getType() {
        return type;
    }

}
