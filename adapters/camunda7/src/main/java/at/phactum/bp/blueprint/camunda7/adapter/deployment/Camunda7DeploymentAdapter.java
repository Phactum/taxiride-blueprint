package at.phactum.bp.blueprint.camunda7.adapter.deployment;

import javax.annotation.PostConstruct;

import org.camunda.bpm.engine.ProcessEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import at.phactum.bp.blueprint.bpm.deployment.ModuleAwareBpmnDeployment;

public class Camunda7DeploymentAdapter extends ModuleAwareBpmnDeployment {

	private static final Logger logger = LoggerFactory.getLogger(Camunda7DeploymentAdapter.class);
	
    private ProcessEngine processEngine;
    
    public Camunda7DeploymentAdapter(
            final ProcessEngine processEngine) {
        
        super();
        this.processEngine = processEngine;
        
    }

    @Override
    protected Logger getLogger() {
    	
    	return logger;
    	
    }
    
    @Override
    @PostConstruct
    public void deployAllWorkflowModules() {

        super.deployAllWorkflowModules();

    }

    @Override
    protected void doDeployment(
    		final String workflowModuleId,
            final Resource[] bpmns,
            final Resource[] dmns,
            final Resource[] cmms)
            throws Exception {

        final var deploymentBuilder = processEngine
                .getRepositoryService()
                .createDeployment()
                .enableDuplicateFiltering(true)
                .source(applicationName)
                .tenantId(workflowModuleId)
                .name(workflowModuleId);

        for (final var resource : bpmns) {
            try (final var inputStream = resource.getInputStream()) {
                deploymentBuilder.addInputStream(resource.getFilename(), inputStream);
            }
        }

        for (final var resource : cmms) {
            try (final var inputStream = resource.getInputStream()) {
                deploymentBuilder.addInputStream(resource.getFilename(), inputStream);
            }
        }

        for (final var resource : dmns) {
            try (final var inputStream = resource.getInputStream()) {
                deploymentBuilder.addInputStream(resource.getFilename(), inputStream);
            }
        }

        deploymentBuilder.deployWithResult();
        
    }
    
}
