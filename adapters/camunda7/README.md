# Camunda 7 adapter

This is an adapter which implements bindings of the Blueprint SPI to [Camunda 7](https://docs.camunda.org/) on order to run business processes.

Camunda 7 is a BPMN engine Java library which processes BPMN files deployed along with Java classes which implement the behavior of BPMN tasks. The engine uses a relational database to persist state and to store history processing data. Therefore proper transaction handling is crucial e.g. to control behavior in special situations like unexpected errors of connected components. Due to the nature of the library and it's persistence Camunda 7 is a good fit up to mid-range scaled use-cases.

This adapter is aware of all the details needed to keep in mind on using Camunda 7 and implements a lot of best practices based on a long years experience.

## Features

### Module aware deployment

To avoid interdependencies between implementation of different use-cases packed into a single microservice the concept of [workflow modules](../spring-boot/README.md#workflow-modules) is introduced. This adapter builds a Camunda deployment for each workflow module found in the classpath.

Since Camunda is not aware of workflow modules the Camunda tenant-id is used to store the relationship between BPMNs and DMNs and their workflow module. As a consequence Camunda's tenant-ids cannot be used to distinguish "real" tenants any more. Beside this, one might introduce groups for Camunda Cockpit which only show resources of one workflow module to keep track easier.

Additionally, in a clustered environment during rolling deployment, to not start jobs by Camunda's job-executor originated by newer deployments add this setting in your application.yaml of the microservice' container:

```yaml
camunda:
   bpm:
      job-execution:
         deployment-aware: true
```

### SPI Binding validation

On starting the application BPMNs of all workflow modules will be wired to the SPI. This includes

1. BPMN files which are part of the current workflow module bundle (e.g. classpath:processes/*.bpmn)
1. BPMN files deployed as part of previous versions of the workflow module
   1. Older versions of BPMN files which are part of the current workflow module bundle
   1. BPMN files which are not part of the current workflow module bundle any more
   
This ensures that correct wiring of all process definitions according to the SPI is done.

### Transaction behavior

