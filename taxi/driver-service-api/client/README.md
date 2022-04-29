# Driver service client

The client is build based on the given [driver service's OpenAPI definition file](../driver-service-api.yaml). Since the driver service's API pushes information asynchronously (callback), the workflow module using this client also has to provide a REST service to receive those updates (see [driver service's callback OpenAPI definition file](../driver-service-callback-api.yaml)). The interface of that callback controller is part of this client module.

Based on the *API-first* paradigm the Java classes are generated using the Maven plugin `openapi-generator-maven-plugin`.

## Usage

### Maven dependency

Add this Maven module as a dependency to your workflow module:

```
<dependency>
  <groupId>at.phactum.bp.blueprint.taxiride</groupId>
  <artifactId>driver-service-api-client</artifactId>
  <version>${project.version}</version>
</dependency>
```

### Client configuration

Your workflow module properties bean class has to implement `com.taxicompany.driver.client.v1.DriverServiceClientAwareProperties`:

```java
@Configuration
@ConfigurationProperties(prefix = "ride")
public class RideProperties
        implements DriverServiceClientAwareProperties {

    private Client driverServiceClient;
    
    @Override
    public Client getDriverServiceClient() {
        return driverServiceClient;
    }

    public void setDriverServiceClient(Client driverServiceClient) {
        this.driverServiceClient = driverServiceClient;
    }
    ...
```

Then you can set client properties in your workflow modules properties file (e.g. `config/ride.yaml`):

```yaml
ride:
  driver-service-client:
    base-url: http://localhost:8081/api/v1
    log: true
```

For a detailed description of all properties look into [Blueprint REST adapter](../../../blueprint-adapters/blueprint-rest-adapter).

### Code sample

The client can be autowired by it's interface:

```java
import com.taxicompany.driver.client.v1.DriverServiceApi;
    ...
    @Autowired
    private DriverServiceApi driverService;
```

The callback service controller should look like this:

```java
import com.taxicompany.driver.service.v1.DriverCallbackApi;

@RestController
@RequestMapping(path = "/api/v1")
public class DriverCallbackApiController implements DriverCallbackApi {
```

and has to override all the methods given by the `DriverCallbackApi` interface.