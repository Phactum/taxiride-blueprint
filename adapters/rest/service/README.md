![Draft](../../../readme/draft.png)
# REST service

To prepare a REST service, code may be generated as part of Maven's "generate sources" phase. As Spring Boot provides all tools necessary to implement REST services, this module simple bundles a couple of dependencies instead of providing Java classes. Beyond that code should be generated based on a given interface specification file.

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
            <id>generate-api-service</id>
            <goals>
              <goal>generate</goal>
            </goals>
            <configuration>
              <inputSpec>${project.basedir}/../driver-service-callback-api.yaml</inputSpec>
              <generatorName>spring</generatorName>
              <apiPackage>com.taxicompany.driver.service.v1</apiPackage>
              <modelPackage>com.taxicompany.driver.service.v1</modelPackage>
              <generateSupportingFiles>true</generateSupportingFiles>
              <configOptions>
                <interfaceOnly>true</interfaceOnly>
                <useSpringController>true</useSpringController>
                <java8>true</java8> <!-- avoid build interface default implementations -->
                <dateLibrary>java8</dateLibrary> <!-- use OffsetDateTime -->
                <enablePostProcessFile>true</enablePostProcessFile>
              </configOptions>
              <addCompileSourceRoot>true</addCompileSourceRoot>
              <generateApiTests>false</generateApiTests>
              <generateModelTests>false</generateModelTests>
              <supportingFilesToGenerate>ApiUtil.java</supportingFilesToGenerate>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
```

Next one has to define a controller which implements the Java interface generated:

```java
@RestController
@RequestMapping(path = "/api/v1")
public class DriverCallbackApiController implements DriverCallbackApi {
    ...
}
```

Additionally, Swagger UI may be enabled by adding a configuration like this:

```java
@Configuration
@EnableSwagger2
public class SwaggerConfiguration implements WebMvcConfigurer {

    @Bean
    @ConditionalOnExpression("#{(systemProperties['swagger.gui.enable'] ?: 'false') == 'true'}")
    public Docket produceSwaggerApi(@Value("${spring.application.name:application}") String appName) {
        ApiInfo apiInfo = new ApiInfo(
                "API (microservice '" + StringUtils.capitalize(appName) + "')",
                "API", "1.0", "", ApiInfo.DEFAULT_CONTACT, "", "", new ArrayList<>());
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo)
                .useDefaultResponseMessages(true)
                .groupName("API")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.taxicompany"))
                .paths(PathSelectors.ant("/api/**"))
                .build();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}
```

*Hint:* The Swagger UI can be disabled (e.g. in production environment) using this configuration property:

```yaml
swagger:
  gui:
    enable: false
```
