package com.taxicompany.driver.callback.client.v1;

import at.phactum.bp.blueprint.modules.WorkflowModuleIdAwareProperties;
import at.phactum.bp.blueprint.rest.adapter.Client;

public interface DriverCallbackServiceClientAwareProperties extends WorkflowModuleIdAwareProperties {

    Client getDriverCallbackServiceClient();

}
