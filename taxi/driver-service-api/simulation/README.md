# Driver service simulation

For local development and integration tests a separate Spring boot container is used to simulate bounded systems like the driver service.

This module consists of the REST controller interface for this simulation generated based on the given [driver service's OpenAPI definition file](../driver-service-api.yaml). Additionally, a REST client used to send callback notifications is provided (see [driver service's callback OpenAPI definition file](../driver-service-callback-api.yaml)).

Based on the *API-first* paradigm the Java classes are generated using the Maven plugin `openapi-generator-maven-plugin`.

## Usage

### Maven dependency

Add this Maven module as a dependency to your workflow module:

```
<dependency>
  <groupId>at.phactum.bp.blueprint.taxiride</groupId>
  <artifactId>driver-service-api-simulation</artifactId>
  <version>${project.version}</version>
</dependency>
```

### Client configuration

Your workflow module simulation properties bean class has to implement `com.taxicompany.driver.callback.client.v1.DriverCallbackServiceClientAwareProperties`:

```java
@Configuration
@ConfigurationProperties(prefix = "ride-simulation")
public class RideSimulationProperties
        implements DriverCallbackServiceClientAwareProperties {

    private Client driverCallbackServiceClient;
    
    @Override
    public Client getDriverCallbackServiceClient() {
        return driverCallbackServiceClient;
    }

    public void setDriverCallbackServiceClient(Client driverCallbackServiceClient) {
        this.driverCallbackServiceClient = driverCallbackServiceClient;
    }
    ...
```

Then you can set client properties in your workflow modules simulation properties file (e.g. `config/ride-simulation.yaml`):

```yaml
ride-simulation:
  driver-callback-service-client:
    base-url: http://localhost:8080/api/v1
    log: true
```

For a detailed description of all properties look into [Blueprint REST adapter](../../../blueprint-adapters/blueprint-rest-adapter).

### Code sample

The client can be autowired by it's interface:

```java
import com.taxicompany.driver.callback.v1.DriverCallbackApi;
    ...
    @Autowired
    private DriverCallbackApi driverCallbackApi;
```

The REST service controller should look like this:

```java
import com.taxicompany.driver.service.v1.DriverApi;

@RestController
@RequestMapping("/api/v1")
public class DriverApiController implements DriverApi {
```

and has to override all the methods given by the `DriverApi` interface.