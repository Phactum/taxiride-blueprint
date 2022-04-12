package com.taxicompany.simulation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.taxicompany.driver.callback.client.v1.DriverCallbackServiceClientAwareProperties;

import at.phactum.bp.blueprint.async.AsyncProperties;
import at.phactum.bp.blueprint.async.AsyncPropertiesAware;
import at.phactum.bp.blueprint.rest.adapter.Client;

@ConfigurationProperties(prefix = "application")
public class ApplicationProperties implements AsyncPropertiesAware, DriverCallbackServiceClientAwareProperties {

    private AsyncProperties async = new AsyncProperties();

    private Client driverCallbackServiceClient;

    @Override
    public AsyncProperties getAsync() {

        return async;

    }

    public void setAsync(AsyncProperties async) {

        this.async = async;

    }

    @Override
    public Client getDriverCallbackServiceClient() {

        return driverCallbackServiceClient;

    }

    public void setDriverCallbackServiceClient(Client driverCallbackServiceClient) {

        this.driverCallbackServiceClient = driverCallbackServiceClient;

    }

    @Override
    public String getWorkflowModuleId() {

        return "simulation";

    }

}
