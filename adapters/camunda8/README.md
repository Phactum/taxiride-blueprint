# Camunda 8 adapter

This is an adapter which implements bindings of the Blueprint SPI to [Camunda 8](https://docs.camunda.io/) on order to run business processes.

Camunda 8 is a BPMN engine which behaves as as a closed system similar to a database. The engine itself uses a cloud native storage concept to enable horizontal scaling. By using the API BPMN can be deployed and processes started. In order to execute BPMN tasks a client has to subscribe and will be pushed if work is available. Next to the BPMN engine other optional components are required to handle user tasks or have to operate the system. Those components do not access the engine's data but rather use exported data stored in Elastic Search. As it's nature suggests it fits good to mid- and high-scaled use-cases.

This adapter is aware of all the details needed to keep in mind on using Camunda 8.

## Features

### Module aware deployment

To avoid interdependencies between implementation of different use-cases packed into a single microservice the concept of [workflow modules](../spring-boot/README.md#workflow-modules) is introduced. This adapter builds a Camunda deployment for each workflow module found in the classpath.

Since Camunda 8 does not provide BPMS meta-data (e.g. historical deployments) the adapter stores everything necessary to ensure correct operation in separate DB tables.

### SPI Binding validation

On starting the application BPMNs of all workflow modules will be wired to the SPI. This includes

1. BPMN files which are part of the current workflow module bundle (e.g. classpath:processes/*.bpmn)
1. BPMN files deployed as part of previous versions of the workflow module
   1. Older versions of BPMN files which are part of the current workflow module bundle
   1. BPMN files which are not part of the current workflow module bundle any more
   
This ensures that correct wiring of all process definitions according to the SPI is done.

### Transaction behavior

