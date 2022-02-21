# Domain Dervice Design

This module is about using DDD patterns to simplify business-process applications. Additionally, a lot of loose-coupling techniques are used minimize weaving between the BPMN and the software.

## Concept

The base idea is to have ***ONE*** service which is responsible for all needs of a business-process. If several services or external-components are required then this single service is a frontend for all of them.

In terms of BPMN there are tasks (e.g. service-task, send-task, etc.) which are wired to methods of that service by name and expressions (e.g. conditional-flows) which are evaluated agains a process-specific domain-entity.

## Usage

### Process-specific domain-entity

A process-engine might give you the ability to use process-variables to store information the workflow needs to fulfill decisions like sequence-flow conditions. This blue-print does not use process-variables but introduces a seperate domain-entity for each process:

```java
@Entity
@Table(name = "APPROVALS_OF_VACATION")
public class ApprovalOfVacation implements WorkflowDomainEntity {
  public int numberOfDays;
}
```

The entity's properties can be used in the process e.g. for conditional sequence-flows:

![Camunda Modeller](./readme/expression_propertiespanel.png)

Reasons for not using process-variables:

1. BPMN
   1. Process-variables have no schema and therefore it can be documented and tested for
   1. Using a domain-entity gives you a schema which can be used as a contract between business people and developers
   1. Type-safety of the information needed by the process
1. Operation of workflows
   1. Historic process-variables needs to be cleaned up which might exhauste your database
   1. Process-variables polute the execution-context because typically they are not cleaned up by the developers
   1. As a default *all* process-variables are copied in the case of call-activities
   1. Schema evolution: The variables may be of complex types which may evolve over the time. Migrating those values is a hard job. 

There are some base-requirements for domain-entities used for workflows:

1. It has to store the workflow-id
1. It has to store the exact version of process the workflow is started with

The blueprint-framework simplifies a couple of things in conjunction with this workflow-entity and therefor a base-class `at.phactum.bp.blueprint.domain.WorkflowDomainEntity` is defined which is used like this:

```java
@Entity
@Table(name = "APPROVALS_OF_VACATION")
public class ApprovalOfVacation implements WorkflowDomainEntity {
  ...
}
```

### Wire up a process

Acording to the [concept](#Concept) and in order to have no reference from BPMN to the implemenation an aspect-oriented approach is used for the binding.

There are two primary use-case for this:
1. Whenever a value needs to be calculated (e.g. x business-days as a timer-event definition)
1. Whenever a task needs to fulfill its functionality (e.g. service-task)

In both cases a method needs to be called (1. to calculate a value; 2. to complete a task). Those methods will be lookup up in a service which is bound to a particular process (and process version).

As a basis for this binding the process-id is used:
![Camunda Modeller](./readme/process_propertiespanel.png)

#### Software-first approach

You may have already a component designed in situations in which not business-people define processes but they are used to execute flows in order to improve readability or maintainablity of the software. In those situations using the component's bean-name as a process-id helps to minimize the wiring-configuration according to the paradigm "convention over configuration":

In Spring Boot the class-name of a component is turned into the bean-name by lower-casing the first character:

* class: *ApprovalOfVacationRequests*
* bean-name: *approvalOfVacationRequests*

If you use the bean-name for the process BPMN's process id then you can wire up the component to the process by simply adding the `@WorkflowService` annotation: 

```java
@Component
@WorkflowService
public class ApprovalOfVacationRequests {
  ...
}
```

#### BPMN-first approach

In case of a given BPMN-business-process the component needs to be mapped by setting the `processId` attribute:

```java
@Component
@WorkflowService(processId = "Process_ApprovalOfVacation")
public class ApprovalOfVacationRequests {
  ...
}
```

It is also allowed to apply multiple annotations if the process-id given by business-people changes over the time:

```java
@Component
@WorkflowService(processId = "Process_ApprovalOfVacation")
@WorkflowService(processId = "Process_NewApprovalOfVacation")
public class ApprovalOfVacationRequests {
  ...
}
```

#### Versioning of BPMN business-processes

In case of doing breaking changes in BPMN over the time you can specify for which versions of BPMN this component can be used for:

```java
@Component
@WorkflowService(version = "<10")
public class ApprovalOfOldVacationRequests {
  ...
}
```

Valid formats:
* missing attribute: all versions
* '*': all versions
* '1': only version "1"
* '1-3': only versions "1", "2" and "3"
* '<3': only versions "1" and "2"
* '<=3': only versions "1", "2" and "3"
* '>3': only versions higher then "3"
* '>=3': only versions higher then or equal to "3"

### Wire a task or a expression

Acording to the [concept](#Concept) and in order to have no reference from BPMN to the implemenation an aspect-oriented approach is used for the binding.

There are two primary use-case for this:
1. Whenever a value needs to be calculated (e.g. x business-days as a timer-event definition) ![Camunda Modeller](./readme/timer_propertiespanel.png)
1. Whenever a task needs to fulfill its functionality (e.g. service-task) ![Camunda Modeller](./readme/task_propertiespanel.png)

In both cases a method of the wired service needs to be called (1. to calculate a value; 2. to complete a task)

#### Domain-entity argument


#### Software-first approach

In this situation the name of the  

```java
@WorkflowTask
public void sendAcceptedMail(ApprovalOfVacation entity) {
  ...
}
```

```java
@WorkflowTask(taskDefinition = "SEND_ACCEPTED")
public void sendAcceptedMail(ApprovalOfVacation entity) {
  ...
}
```
