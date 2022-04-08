package com.taxicompany.driver.client.v1;

import at.phactum.bp.blueprint.modules.WorkflowModuleIdAwareProperties;
import at.phactum.bp.blueprint.rest.adapter.Client;

public interface DriverServiceClientAwareProperties extends WorkflowModuleIdAwareProperties {

    Client getDriverServiceClient();

}
