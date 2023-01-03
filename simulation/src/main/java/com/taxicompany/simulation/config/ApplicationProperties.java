package com.taxicompany.simulation.config;

import at.phactum.bp.blueprint.rest.adapter.Client;
import com.taxicompany.driver.callback.client.v1.DriverCallbackServiceClientAwareProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application")
public class ApplicationProperties implements DriverCallbackServiceClientAwareProperties {

    private Client driverCallbackServiceClient;

    @Override
    public Client getDriverCallbackServiceClient() {

        return driverCallbackServiceClient;

    }

    public void setDriverCallbackServiceClient(Client driverCallbackServiceClient) {

        this.driverCallbackServiceClient = driverCallbackServiceClient;

    }

}
