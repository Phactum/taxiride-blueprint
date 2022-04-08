package org.blueprint.bp.blueprint.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import at.phactum.bp.blueprint.bpm.deployment.BpDeploymentConfiguration;
import at.phactum.bp.blueprint.modules.ModuleSpecificProperties;
import at.phactum.bp.blueprint.modules.WorkflowModuleIdAwareProperties;

@Configuration
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties implements BpDeploymentConfiguration, WorkflowModuleIdAwareProperties {

    public static final String WORKFLOW_MODULE_ID = "test1";

    @Bean
    public static ModuleSpecificProperties moduleProps() {

        return new ModuleSpecificProperties(ApplicationProperties.class, WORKFLOW_MODULE_ID);

    }

    private String processesLocation;

    @Override
    public String getProcessesLocation() {
        return processesLocation;
    }

    public void setProcessesLocation(String processesLocation) {
        this.processesLocation = processesLocation;
    }

    @Override
    public String getWorkflowModuleId() {
        return WORKFLOW_MODULE_ID;
    }

}
