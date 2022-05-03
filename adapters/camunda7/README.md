# Camunda 7 adapter

This is an adapter to use [Camunda 7](https://docs.camunda.org/) to run business processes.

Camunda 7 is a BPMN engine Java library which processes BPMN files deployed along with Java classes which implement the behavior of BPMN tasks. The engine uses a relational database to persist state and to store history processing data. Therefore proper transaction handling is crucial controlling behavior in special situations like unexpected errors of connected components. Due to the nature of the library and it's persistence Camunda 7 is a good fit up to mid-range scaled use-cases.

This adapter is aware of all the details needed to keep in mind on using Camunda 7 and implements a lot of best practices based on a long years experience.

## Implemented "best practices"
