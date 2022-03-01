package at.phactum.bp.blueprint.camunda7.adapter;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import at.phactum.bp.blueprint.bpm.deployment.ModuleAwareBpmnDeployment;

public abstract class Camunda7DeploymentAdapter extends ModuleAwareBpmnDeployment<DeploymentWithDefinitions> {

    @Autowired
    private ProcessEngine processEngine;

    protected Camunda7DeploymentAdapter() {
        
        super(null);
        
    }

    protected Camunda7DeploymentAdapter(final String workflowModuleId) {
        
        super(workflowModuleId, "workflows");
        
    }

    protected Camunda7DeploymentAdapter(final String workflowModuleId, final String basePackageName) {

        super(workflowModuleId, basePackageName);

    }

    @Override
    protected DeploymentWithDefinitions doDeployment(
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
                .name(workflowModuleId + " [core]");

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

        return deploymentBuilder.deployWithResult();
        
    }
    
}
