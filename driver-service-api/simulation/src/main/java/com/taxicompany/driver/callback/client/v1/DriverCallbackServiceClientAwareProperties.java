package com.taxicompany.driver.callback.client.v1;

import at.phactum.bp.blueprint.rest.adapter.Client;

/**
 * Each workflow module's simulation properties bean which uses the driver
 * callback REST client has to implement this interface. The client properties
 * provided are used to configure the REST client.
 */
public interface DriverCallbackServiceClientAwareProperties {

    /**
     * @return The driver callback REST client's configuration properties
     */
    Client getDriverCallbackServiceClient();

}
