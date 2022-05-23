![Draft](../readme/draft.png)
# Adapters

There are two groups of adapters which are part of the Blueprint:

1. Engine adapters: To bind the Blueprint SPI to a particular BPMN engine
1. Workflow module adapters: Useful adapters for building workflow modules

## Workflow module adapters

### Spring Boot applications

Extensions to simplify development of Spring Boot based workflow module applications. Read [more...](./spring-boot/README.md)

### REST adapters

A pattern to consume or provide REST APIs using the API first paradigm. Read [more...](./rest/README.md)

## Engine adapters

The Blueprint SPI is designed to hide the details and decouple from a used BPMN engine's API. Reasons for this are listed in the ["Paradigms" section](../spi/README.md#paradigms) of the [SPI's README](../spi/README.md).

### Deployment

An engine adapter has to pickup the bundled BPMN and DMN files and deploy them to the bound engine. Additionally, the files are evaluated against the business code implementation to highlight unbound tasks at startup-time.

### Supported BPMS

1. [Camunda 7](./camunda7/README.md)
1. [Camunda 8](./camunda8/README.md)
