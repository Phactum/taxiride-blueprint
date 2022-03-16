# Blueprint for Business-Processing Services

This project is about building Java-based business-processing (micro)services.

It incorporates best-practices collected as part of business-processing services developed since 2014.

## Paradigms

### Loose-coupling

On one hand for designing a business-process one needs to understand what the business is about and what kind of tasks and values are used to fulfill the need of the process. On the other hand one should not need to know about the details of the underlying implementing software.

Using loose-coupling in varios ways helps you to understand and express the contract between the business-people and the development. This means that there is no reference from BPMN to the implementation besides that contract.

As a side-effect of doing loose-coupling the outcoming software is much easier to understand und to maintain over the time.

### Domain-Drive-Design

To support the entire lifecycle of a software in a good way, it turned out that loose-coupling the business-processing engine itself should be preferred. A good fit for this is to use a domain-driven-design approach.

Check out [this module](./ddd/README.md) to learn about how this looks like.

### Runtime environment

The blueprint is based on Spring Boot, but it can be easily adopted to Java Enterprise as well.

## Nomenclature

Designing software isn't just about coding in the form of a programming language or writing configuration files. It's also a lot about communication - both with other developers and with business people. A few wording conventions can help to simplify things.

### Processes versus Workflows

On developing business-processing based software a lot of communication to business people is necessary. This kind of close communication is one of the big advantages when using BPMN as a common language. It is used to define the process and can be reused for execution.

On running business-processing based software operational tasks will not be about the process (it's definition) but about a certain instance of that process which caused that task. In human conversation, the term "process" is often used misleadingly to refer to the instance of the process instance what makes things complicated.

As a solution for this confusion the term "workflow" is introduced replace "process instance". So, whenever one is talking about a "process" then the process definition (e.g. the BPMN) is meant and if one is talking about a "workflow" a certain process instance is meant.

### The "blueprint-framework"

A couple of aspect-oriented designs are used. The implementation of them is called the "blueprint-framework".

### Examples

All examples refered in the README files are about a process which is about approval of vacation requests.
