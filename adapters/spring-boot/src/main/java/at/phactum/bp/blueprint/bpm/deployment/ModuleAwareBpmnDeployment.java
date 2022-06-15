package at.phactum.bp.blueprint.bpm.deployment;

import static java.lang.String.format;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import at.phactum.bp.blueprint.modules.ModuleSpecificProperties;
import at.phactum.bp.blueprint.modules.WorkflowModuleIdAwareProperties;

public abstract class ModuleAwareBpmnDeployment {

	public static final String DEFAULT_BASE_PACKAGE_NAME = "processes";
	
    protected abstract Logger getLogger();

    @Value("${spring.application.name}")
    protected String applicationName;

    @Autowired(required = false)
    private List<ModuleSpecificProperties> moduleProperties;
    
    @Autowired(required = false)
    private List<BpDeploymentConfiguration> properties;
    
    protected void deployAllWorkflowModules() {
    	
    	if (moduleProperties == null) {
    		getLogger().warn("No workflow-module properties defined using 'ModuleSpecificProperties'");
    		return;
    	}
    	
    	moduleProperties.forEach(this::deployWorkflowModule);
    	
    }
    
    private void deployWorkflowModule(
    		final ModuleSpecificProperties propertySpecification) {
    	
        final var moduleProperties = moduleProperties(propertySpecification);
    	final var basePackageName = determineBasePackageName(moduleProperties);
    	final var workflowModuleId = propertySpecification.getName();
    	
        deployWorkflowModule(
                workflowModuleId,
                basePackageName,
                (WorkflowModuleIdAwareProperties) moduleProperties.orElse(null));
    	
    }
    
    private Optional<BpDeploymentConfiguration> moduleProperties(
            final ModuleSpecificProperties propertySpecification) {

        if (properties == null) {
            return Optional.empty();
        }
        
        return properties
                .stream()
                .filter(p -> propertySpecification.getPropertiesClass().isAssignableFrom(p.getClass()))
                .findFirst();
        
    }
    
    private String determineBasePackageName(
            final Optional<BpDeploymentConfiguration> moduleProperties) {
    	
    	if (moduleProperties.isEmpty()) {
    		return DEFAULT_BASE_PACKAGE_NAME;
    	}
    	
    	return moduleProperties
    	        .map(BpDeploymentConfiguration::getProcessesLocation)
    			.orElse(DEFAULT_BASE_PACKAGE_NAME);
    	
    }

    protected abstract void doDeployment(
    		String workflowModuleId,
            WorkflowModuleIdAwareProperties properties,
    		Resource[] bpmns,
    		Resource[] dmns,
    		Resource[] cmms) throws Exception;

    private void deployWorkflowModule(
    		final String workflowModuleId,
    		final String basePackageName,
            final WorkflowModuleIdAwareProperties moduleProperties) {
    	
        try {

            final var bpmns = findResources(workflowModuleId, basePackageName, "*.bpmn");
            final var cmms = findResources(workflowModuleId, basePackageName, "*.cmmn");
            final var dmns = findResources(workflowModuleId, basePackageName, "*.dmn");

            doDeployment(workflowModuleId, moduleProperties, bpmns, dmns, cmms);

            getLogger()
                    .info("Deployed resources for process archive <{}>", workflowModuleId);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
        	throw new RuntimeException(e);
        }

    }

    private Resource[] findResources(
            final String workflowModuleId,
            final String basePackageName,
            final String fileNamePattern) throws IOException {

        final var resourcesPath = format("%s%s/**/%s",
                ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX,
                basePackageName.replace('.', '/'),
                fileNamePattern);

        getLogger()
                .debug("Scanning process archive <{}> for {}", workflowModuleId, resourcesPath);

        return new PathMatchingResourcePatternResolver().getResources(resourcesPath);

    }

}
