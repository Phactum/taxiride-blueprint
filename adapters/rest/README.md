# REST adapters

Most of the APIs external systems provide are REST based APIs. In the past SOAP services dominated but that changed over the time.

Meanwhile service definitions like [OpenAPI](https://swagger.io/specification/) or [Swagger](https://swagger.io/specification/v2/) are provided by the teams responsible for APIs which can be used to generate code instead of building JSON requests and parsing JSON responses in the code. That keeps the business code readable and maintainable.

Accessing external services is always the same procedure:

1. Generate a client based on the service definition file
1. Configure that client regarding
   1. define environment specific endpoint of the service
   1. set connect- and read-timeouts
   1. do proxy settings, if required
   1. enable logging of traffic
   1. configure authentication (e.g. basic, oauth, keycloak, etc.)
1. Inject that client into your business service
1. Handle authentication

The [client module](./client/README.md) provides code which can be used to handle all of these tasks transparently with nearly zero coding.

The [service module](./service/README.md) can be used to provide REST endpoints either as part of your service (e.g. for asynchronous notification/callbacks of external systems) or as part of a simulation container.

*simulation*

Although we strongly suggest to integrate early, in practice it's often a long way to get external services available (in test or even in production environments). To be not blocked building features based on external services not available yet, a "simulation" enables the developers to move on.

Therefore it turns out as a good practice to develop an additional Spring Boot container we call "simulation" which implements the interfaces of all external systems (e.g. REST-APIs, embedded LDAP instead of ActiveDirectory, embedded Kafka, etc.) to be used for local development as well as for running integration tests as part of the build. The simulation's behavior should be as close to the external system as possible (especially regarding authentication used to protect those APIs) but also as simple as possible.

Typically for the services consumed, we implement simulation of major use cases in a simplified way using static data. Sometimes we add behavior according to test cases based on input data used for the test cases. For the taxi driver service consumed as part of the Blueprint sample "taxi ride" a random number of drivers are reported as available and within a couple of seconds an asynchronous ride confirmation for one of them is sent (investigate [example](../../taxi/simulation)).
