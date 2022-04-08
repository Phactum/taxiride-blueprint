package com.taxicompany.ride.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.taxicompany.driver.client.v1.DriverServiceClientAwareProperties;

import at.phactum.bp.blueprint.bpm.deployment.BpDeploymentConfiguration;
import at.phactum.bp.blueprint.modules.ModuleSpecificProperties;
import at.phactum.bp.blueprint.modules.WorkflowModuleIdAwareProperties;
import at.phactum.bp.blueprint.rest.adapter.Client;

@Configuration
@ConfigurationProperties(prefix = "ride")
public class RideProperties
        implements BpDeploymentConfiguration, DriverServiceClientAwareProperties, WorkflowModuleIdAwareProperties {

    public static final String WORKFLOW_MODULE_ID = "ride";

    @Bean
    public static ModuleSpecificProperties moduleProps() {

        return new ModuleSpecificProperties(RideProperties.class, WORKFLOW_MODULE_ID);

    }

    private String processesLocation;

    private Client driverServiceClient;

    @Override
    public String getProcessesLocation() {
        return processesLocation;
    }

    public void setProcessesLocation(String processesLocation) {
        this.processesLocation = processesLocation;
    }

    @Override
    public Client getDriverServiceClient() {
        return driverServiceClient;
    }

    public void setDriverServiceClient(Client driverServiceClient) {
        this.driverServiceClient = driverServiceClient;
    }

    @Override
    public String getWorkflowModuleId() {
        return WORKFLOW_MODULE_ID;
    }

}
