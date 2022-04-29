package com.taxicompany.driver.client.v1;

import at.phactum.bp.blueprint.modules.WorkflowModuleIdAwareProperties;
import at.phactum.bp.blueprint.rest.adapter.Client;

/**
 * Each workflow module's properties bean which uses the driver service REST
 * client has to implement this interface. The client properties provided are
 * used to configure the REST client.
 */
public interface DriverServiceClientAwareProperties extends WorkflowModuleIdAwareProperties {

    /**
     * @return The driver service REST client's configuration properties
     */
    Client getDriverServiceClient();

}
