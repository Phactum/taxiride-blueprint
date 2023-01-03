# Taxi Ride Blueprint for Business-Processing (Micro)Services

As a [consulting company](#noteworthy-contributors) we supported a lot of people bringing BPMN into their companies using the BPMN engine [Camunda 7](https://docs.camunda.com). As Camunda introduced their new major release [Camunda 8](https://docs.camunda.io) in April 2022 we built this Blueprint to support users of Camunda on their way from Camunda 7 to 8. Since the new release has a complete new API the Blueprint follows the advice of Camunda to [separate the business logic from the engine's API](https://docs.camunda.io/docs/guides/migrating-from-camunda-platform-7/#prepare-for-smooth-migrations) by introducing an aspect oriented service provider interface (SPI) called [VanillaBP](https://github.com/Phactum/vanilla-bp-spi-for-java).

More than this, this Blueprint is for developing Java based business process driven (micro)services. We do this by a sample workflow about processing a taxi ride. You can use it to build your own application by either clone and adopt the taxi ride or by using the underlying modules as Maven dependencies. **The Blueprint should help you building maintainable business processing (micro)services with minimal effort**.

*If you are not familiar with BPMN:* BPMN is a graphical representation for specifying business processes in XML also including semantic information. A BPMN engine runs those processes and acts as a state engine. This helps to dramatically reduce the amount of code since only "tasks" need to be implemented and the flow is handled by the engine. Hang on to see this in action.

## Content

1. [How it looks like](#how-it-looks-like)
1. [Motivation](#motivation)
1. [Prerequisites](#prerequisites)
1. [See the taxi-ride in action](#see-the-taxi-ride-in-action)
1. Modules of the Blueprint:
     1. *[ride](./ride/README.md):* A workflow module about the taxi ride
     1. *container:* A Spring Boot container hosting the *ride* workflow module
     1. *simulation:* A Spring Boot container simulating all bounded APIs needed by workflow module (e.g. a Driver service REST API)
     1. *[rest](./rest/README.md):* A couple of classes supporting OpenAPI based API-first REST APIs
     1. *[driver-service-api](./driver-service-api/README.md):* An example of a OpenAPI based REST API used by the workflow module
1. [Noteworthy & Contributors](#noteworthy-contributors)
1. [License](#license)

## How it looks like

This is a section of a taxi ride workflow and should give you an idea of how the [VanillaBP SPI](https://github.com/Phactum/vanilla-bp-spi-for-java) is used in your business code:

![Section of a taxi ride workflow](./readme/example.png)

```java
@Service
@WorkflowService(workflowAggregateClass = Ride.class)
@Transactional(noRollbackFor = TaskException.class)
public class TaxiRide {
    
    @Autowired
    private ProcessService<Ride> processService;
    
    public String rideBooked(
            final Location pickupLocation,
            final OffsetDateTime pickupTime,
            final Location targetLocation) {
        
        final var ride = new Ride();
        ...
        // start the workflow by correlation of the message start event
        return processService
                .correlateMessage(ride, "RideBooked")
                .getRideId();
    }
    
    @WorkflowTask
    public void determinePotentialDrivers(
            final Ride ride) {
        
        final var parameters = new DriversNearbyParameters()
                .longitude(ride.getPickupLocation().getLongitude())
                .latitude(ride.getPickupLocation().getLatitude());

        final var potentialDrivers = driverService
                .determineDriversNearby(parameters);

        ride.setPotentialDrivers(
                mapper.toDomain(potentialDrivers, ride));
    }

    @WorkflowTask
    public void requestRideOfferFromDriver(
            final Ride ride,
            @MultiInstanceIndex("RequestRideOffer")
            final int potentialDriverIndex) {
        
        final var driver = ride.getPotentialDrivers().get(potentialDriverIndex);
        
        driverService.requestRideOffer(
                driver.getId(),
                new RequestRideOfferParameters()
                        .rideId(ride.getRideId())
                        .pickupLocation(mapper.toApi(ride.getPickupLocation()))
                        .pickupTime(ride.getPickupTime())
                        .targetLocation(mapper.toApi(ride.getTargetLocation())));
        
    }
    ...
```

For more details read each module's description link in the [content](#content) section.

## Motivation

Each BPMN engine, also called Business Processing Management System (BPMS) or workflow system, has its own APIs. Using a workflow system requires a developer to know the API and also to understand its paradigms. Typically, the API is not completely decoupled from the runtime environment which means that things like transactional behavior, synchronization of concurrent executions and similar has to be controlled by the business code but also affects the behavior of the workflow system.

To deal with this one should follow clean architecture patterns by separating the business logic from the workflow engine's API. This is a good idea to keep your software simple, maintainable and easy to migrate, but on the other hand this is also a burden to implement. In addition, for beginners it is difficult to learn and apply both at the same time: how to structure software driven by a workflow engine and how to deal with the aforementioned details.

To deal with the problems mentioned we decided to define **an SPI (service provider interface) for workflow systems as a we would expect it to be** which we called *[VanillaBP](https://github.com/Phactum/vanilla-bp-spi-for-java)*. This SPI incorporates best-practices collected as part of developing business-processing services since 2014 using various of those system.

As part of this Blueprint we provide implementations of the SPI which are called adapters and hide all the details of a workflow system API. This lets the developer focus on the business aspects rather than technical details.

As evidence we provide adapters for some Camunda 7 and Camunda 8 as well as optional support for workflow modules in a microservice environment.

## Prerequisites

You should know about [BPMN](https://en.wikipedia.org/wiki/Business_Process_Model_and_Notation) and you should be able to create meaningful models using a [modeler tool](https://camunda.com/en/download/modeler/). You also need to be able to work with the tool-stack of the Blueprint: Java, Spring Boot and  Maven.

## See the taxi-ride in action

If you want to use the taxi-ride workflow for demo purposes then follow these instructions in the directory of your Git `taxiride-blueprint` clone:

### Camunda 7

Start all taxi-ride services:

```sh
# Build using Camunda 7 dependencies
mvn clean package -Pcamunda7
# Run the simulation of bounded systems
java -jar simulation/target/taxi-simulation-1.0.0-SNAPSHOT.jar >/tmp/simulation.log &
# Run the service
java -Dspring.profiles.active=local,camunda7 -jar container/target/taxi-container-1.0.0-SNAPSHOT.jar
```

Checkout Camunda 7 [cockpit](http://localhost:8080/camunda/app/) and login (username: admin, password: admin). There will be no running workflows.

Use [Postman](https://www.postman.com/) to send requests to the system. Import collection `TaxiRideBlueprint.postman_collection.json` and send request `ride booked`. The response body shows the id of the workflow started.

Refreshing `Camunda cockpit` in the browser should now show you that running workflow.

After a while the workflow runs into the usertask `Retrieve payment`. To complete this task use request `ride charged` in Postman.

Refreshing `Camunda cockpit` in the browser again should now show you that the workflow is gone since it completed successfully.

### Camunda 8

Download file [Camunda 8 docker-compose file](https://github.com/camunda/camunda-platform/blob/main/docker-compose-core.yaml) and run it to start Camunda 8:

```sh
docker-compose -f docker-compose-core.yaml up -d
```

Start all taxi-ride services:

```sh
# Build using Camunda 8 dependencies
mvn clean package -Pcamunda8
# Run the simulation of bounded systems
java -jar simulation/target/taxi-simulation-1.0.0-SNAPSHOT.jar >/tmp/simulation.log &
# Run the service
java -Dspring.profiles.active=local,camunda8 -jar container/target/taxi-container-1.0.0-SNAPSHOT.jar
```

Checkout Camunda 8 [operate](http://localhost:8081) and login (username: demo, password: demo). There will be no running workflows.

Use [Postman](https://www.postman.com/) to send requests to the system. Import collection `TaxiRideBlueprint.postman_collection.json` and send request `ride booked`. The response body shows the id of the workflow started.

Refreshing `Camunda operate` in the browser should now show you that running workflow.

After a while the workflow runs into the usertask `Retrieve payment`. To complete this task use request `ride charged` in Postman.

Refreshing `Camunda operate` in the browser again should now show you that the workflow is gone since it completed successfully.

## Noteworthy & Contributors

This Taxi Ride Blueprint was developed by [Phactum](https://www.phactum.at) with the intention of giving back to the community as it has benefited the community in the past.

![Phactum](./readme/phactum.png)


## License

Copyright 2022 Phactum Softwareentwicklung GmbH

Licensed under the Apache License, Version 2.0
