package at.phactum.bp.blueprint.modules;

import at.phactum.bp.blueprint.utilities.CaseUtils;

public class ModuleSpecificProperties {
	
	private Class<?> propertiesClass;
	
    private String name;

    public ModuleSpecificProperties(
    		final Class<?> propertiesClass,
    		final String name) {
    	
    	this.propertiesClass = propertiesClass;
        this.name = name;
        
    }

    public ModuleSpecificProperties(
    		final Class<?> propertiesClass,
    		final String name,
    		final boolean isCamelCase) {
    	
    	this.propertiesClass = propertiesClass;
        if (isCamelCase) {
            this.name = CaseUtils.camelToKebap(name);
        } else {
            this.name = name;
        }
        
    }

    public String getName() {
    	
        return name;
        
    }
    
    public Class<?> getPropertiesClass() {
		
    	return propertiesClass;
    	
	}
    
}