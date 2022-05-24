![Draft](../../../readme/draft.png)
# REST client

To prepare a REST client, code needs to be generated as part of Maven's "generate sources" phase. The REST client base classes provided by this module can be used to configure the code generated for runtime.  

Using OpenAPI generator Maven plugin generating code my look like this:

```xml
  <build>
    <plugins>
      <plugin>
        <groupId>org.openapitools</groupId>
        <artifactId>openapi-generator-maven-plugin</artifactId>
        <version>5.4.0</version>
        <executions>
          <execution>
            <id>generate-api-client</id>
            <goals>
              <goal>generate</goal>
            </goals>
            <configuration>
              <inputSpec>${project.basedir}/../driver-service-api.yaml</inputSpec>
              <generatorName>java</generatorName> <!-- build a Java client -->
              <apiPackage>com.taxicompany.driver.client.v1</apiPackage>
              <modelPackage>com.taxicompany.driver.client.v1</modelPackage>
              <invokerPackage>com.taxicompany.driver.client.v1</invokerPackage>
              <configOptions>
                <java8>true</java8><!-- avoid build interface default implementations -->
                <dateLibrary>java8</dateLibrary><!-- use OffsetDateTime -->
                <ensureUniqueParams>false</ensureUniqueParams>
                <useGzipFeature>true</useGzipFeature>
              </configOptions>
              <library>feign</library> <!-- use feign HTTP library -->
              <addCompileSourceRoot>true</addCompileSourceRoot>
              <generateApiTests>false</generateApiTests>
              <generateModelTests>false</generateModelTests>
              <supportingFilesToGenerate> <!-- skip unneeded generated files -->
                 RFC3339DateFormat.java,
                 StringUtil.java,
                 ApiResponse.java,
                 ApiClient.java,
                 EncodingUtils.java,
                 HttpBasicAuth.java,
                 ApiResponseDecoder.java,
                 ApiKeyAuth.java,
                 HttpBearerAuth.java
              </supportingFilesToGenerate>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
```

Next one has to define a Java interface used to inject application properties into the client configuration bean:

```java
public interface DriverServiceClientAwareProperties {
    Client getDriverServiceClient();
}
```

This interface needs to be implemented by a Spring properties class e.g.

```java
@ConfigurationProperties(prefix = "application")
public class ApplicationProperties implements DriverServiceClientAwareProperties {
    private Client driverServiceClient;
    ...
```

According to the class `at.phactum.bp.blueprint.rest.adapter.Client` a client's environment specific configuration may look like this:

```yaml
application:
  driver-service.client:
    base-url: https://driver-service.com/api
    connect-timeout: 3000    # in ms; default: 1500
    read-timeout: 15000      # in ms; default: 10000
    log: true                # log request/response; default: false
                             # verification of https certificates; default: true
    verify-ssl: false        # may be used for self-signed-certificates
                             # truststore used for https certificate validation
                             # default: none
    ssl-truststore-filename: /etc/mycompany.pkcs12
    ssl-truststore-password: 1234
    proxy:                   # default: no proxy
      host: myproxy.com
      port: 3128
      username: super
      password: secure
    authentication:          # default: none
      basic: true            # use basic auth; default false
      username: abc          # for basic auth
      password: 123          # for basic auth
      oauth:                 # if oauth instead of basic
        client-id: 123456
        client-secret: ABC
        basic: true          # pass client-credentials as basic auth; default: false
```

All these configuration properties are taken into account if the method `configureFeignBuilder` of the class `ClientsConfigurationBase` is used to build the client:

```java
@Configuration
public class ClientsConfiguration extends ClientsConfigurationBase {
    @Autowired private DriverServiceClientAwareProperties properties;
    
    @Bean
    public DriverApi driverServiceApi() {
        final var client = properties.getDriverServiceClient();
        final var apiClient = new ApiClient();
        apiClient.setBasePath(client.getBaseUrl());

        configureFeignBuilder(
                DriverServiceApi.class,
                apiClient.getFeignBuilder(),
                client);
        
        return apiClient.buildClient(DriverServiceApi.class);
    }
}
```

Doing so the client can be injected in a business service class by the API's generated Java interface:

```java
@Service
public class TaxiRide {
   @Autowired
   private DriverApi driverService;
   ...
}
```

*Hint:* Using a client built like this handles token based authentication transparently if configured. So, the any request having no token and upcoming request having expired tokens will be retried using a newly fetched token.
