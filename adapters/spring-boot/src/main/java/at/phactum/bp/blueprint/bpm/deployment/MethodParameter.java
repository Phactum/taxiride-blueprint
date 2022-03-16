package at.phactum.bp.blueprint.bpm.deployment;

public class MethodParameter {

    public static enum Type {
        UNKNOWN,
        DOMAINENTITY,
        MULTIINSTANCE_ELEMENT,
        MULTIINSTANCE_RESOLVER,
        MULTIINSTANCE_INDEX,
        MULTIINSTANCE_TOTAL
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
