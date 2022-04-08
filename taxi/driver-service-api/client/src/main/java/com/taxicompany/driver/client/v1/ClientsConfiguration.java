package com.taxicompany.driver.client.v1;

import java.util.List;

import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import at.phactum.bp.blueprint.rest.adapter.ClientsConfigurationBase;
import at.phactum.bp.blueprint.utilities.BeanUtils;

@Configuration
public class ClientsConfiguration extends ClientsConfigurationBase {

    @Autowired
    private List<DriverServiceClientAwareProperties> allProperties;

    @Bean
    @Scope("prototype")
    public DriverServiceApi driverService(
            final InjectionPoint injectionPoint) {
        
        final var properties = getProperties(
                injectionPoint, DriverServiceClientAwareProperties.class, allProperties);
        
        final var client = properties.getDriverServiceClient();
        if (client == null) {
            final var propertiesClass = BeanUtils.targetClass(properties);
            final var annotation = propertiesClass.getAnnotation(ConfigurationProperties.class);
            throw new RuntimeException(
                    "no section '"
                    + (annotation != null ? annotation.prefix() : "")
                    + ".driver-service-client' in yaml file of '"
                    + propertiesClass.getName()
                    + "'!");
        }

        final ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(client.getBaseUrl());

        configureFeignBuilder(
                DriverServiceApi.class,
                apiClient.getFeignBuilder(),
                client);
        
        return apiClient.buildClient(DriverServiceApi.class);
        
    }

}
