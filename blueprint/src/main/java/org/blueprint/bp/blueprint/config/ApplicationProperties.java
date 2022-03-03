package org.blueprint.bp.blueprint.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import at.phactum.bp.blueprint.bpm.deployment.BpDeploymentConfiguration;
import at.phactum.bp.blueprint.modules.ModuleSpecificProperties;

@Configuration
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties implements BpDeploymentConfiguration {

    @Bean
    public static ModuleSpecificProperties moduleProps() {

        return new ModuleSpecificProperties(ApplicationProperties.class, "test");

    }

    private String processesLocation;

    @Override
    public String getProcessesLocation() {
        return processesLocation;
    }

    public void setProcessesLocation(String processesLocation) {
        this.processesLocation = processesLocation;
    }

}
