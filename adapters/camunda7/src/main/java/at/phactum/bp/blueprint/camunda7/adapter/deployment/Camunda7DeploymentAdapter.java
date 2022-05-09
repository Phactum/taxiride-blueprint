package at.phactum.bp.blueprint.camunda7.adapter.deployment;

import javax.annotation.PostConstruct;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.ResumePreviousBy;
import org.camunda.bpm.engine.spring.application.SpringProcessApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import at.phactum.bp.blueprint.bpm.deployment.ModuleAwareBpmnDeployment;

public class Camunda7DeploymentAdapter extends ModuleAwareBpmnDeployment {

	private static final Logger logger = LoggerFactory.getLogger(Camunda7DeploymentAdapter.class);
	
    private final ProcessEngine processEngine;
    
    private final SpringProcessApplication processApplication;

    public Camunda7DeploymentAdapter(
            final SpringProcessApplication processApplication,
            final ProcessEngine processEngine) {
        
        super();
        this.processEngine = processEngine;
        this.processApplication = processApplication;
        
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
                .createDeployment(processApplication.getReference())
                .resumePreviousVersions()
                .resumePreviousVersionsBy(ResumePreviousBy.RESUME_BY_DEPLOYMENT_NAME)
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

        // BPMNs which are new will be parsed and wired as part of the deployment
        deploymentBuilder.deploy();

        // BPMNs which were deployed in the past need to be forced to be parsed for wiring 
        processEngine
                .getRepositoryService()
                .createProcessDefinitionQuery()
                .tenantIdIn(workflowModuleId)
                .list()
                .forEach(definition -> {
                    // process models parsed during deployment are cached and therefore
                    // not wired twice.
                    processEngine.getRepositoryService().getProcessModel(definition.getId());
                });
        
    }
    
}
