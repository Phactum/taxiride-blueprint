package com.taxicompany.ride.config;

import at.phactum.bp.blueprint.rest.adapter.Client;
import com.taxicompany.driver.client.v1.DriverServiceClientAwareProperties;
import io.vanillabp.springboot.adapter.BpDeploymentConfiguration;
import io.vanillabp.springboot.modules.WorkflowModuleIdAwareProperties;
import io.vanillabp.springboot.modules.WorkflowModuleProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = RideProperties.WORKFLOW_MODULE_ID)
public class RideProperties
        implements BpDeploymentConfiguration, DriverServiceClientAwareProperties, WorkflowModuleIdAwareProperties {

    public static final String WORKFLOW_MODULE_ID = "ride";

    @Bean
    public static WorkflowModuleProperties rideModuleProperties() {

        return new WorkflowModuleProperties(RideProperties.class, WORKFLOW_MODULE_ID);

    }

    private String processesLocation;

    private Client driverServiceClient;

    private Duration periodForImmediatelyPickups;

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

    public Duration getPeriodForImmediatelyPickups() {
        return periodForImmediatelyPickups;
    }

    public void setPeriodForImmediatelyPickups(Duration periodForImmediatelyPickups) {
        this.periodForImmediatelyPickups = periodForImmediatelyPickups;
    }

}
