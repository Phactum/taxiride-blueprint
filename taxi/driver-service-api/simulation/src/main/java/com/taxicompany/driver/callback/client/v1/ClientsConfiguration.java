package com.taxicompany.driver.callback.client.v1;

import java.util.List;

import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.taxicompany.driver.callback.v1.ApiClient;
import com.taxicompany.driver.callback.v1.DriverCallbackApi;

import at.phactum.bp.blueprint.rest.adapter.ClientsConfigurationBase;
import at.phactum.bp.blueprint.utilities.BeanUtils;

@Configuration
public class ClientsConfiguration extends ClientsConfigurationBase {

    @Autowired
    private List<DriverCallbackServiceClientAwareProperties> allProperties;

    @Bean
    @Scope("prototype")
    public DriverCallbackApi driverCallbackService(
            final InjectionPoint injectionPoint) {
        
        final var properties = getProperties(
                injectionPoint, DriverCallbackServiceClientAwareProperties.class, allProperties);
        
        final var client = properties.getDriverCallbackServiceClient();
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
                DriverCallbackApi.class,
                apiClient.getFeignBuilder(),
                client);
        
        return apiClient.buildClient(DriverCallbackApi.class);
        
    }

}
