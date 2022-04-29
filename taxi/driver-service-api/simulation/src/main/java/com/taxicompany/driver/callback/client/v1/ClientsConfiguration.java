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

/**
 * Builds a driver callback REST-client for each injection point based on the
 * workflow module's simulation properties to configure attributes like URL,
 * logging, authentication, etc.
 *
 * @see DriverCallbackServiceClientAwareProperties
 */
@Configuration
public class ClientsConfiguration extends ClientsConfigurationBase {

    /**
     * Properties beans used by various workflow modules.
     * 
     * @see {@link ClientsConfigurationBase#getProperties(InjectionPoint, Class, List)}
     */
    @Autowired
    private List<DriverCallbackServiceClientAwareProperties> allProperties;

    /**
     * Build a driver callback REST client. If this is autowired by different
     * workflow modules then multiple client beans are built using the respective
     * workflow module's properties bean for configuration (which has to implement
     * {@link DriverServiceClientAwareProperties}).
     * 
     * @param injectionPoint The injection
     * @return The REST client
     */
    @Bean
    @Scope("prototype")
    public DriverCallbackApi driverCallbackService(
            final InjectionPoint injectionPoint) {
        
        /*
         * Find the right properties in workflow module of the given injection point
         */
        
        final var properties = getProperties(
                injectionPoint,
                DriverCallbackServiceClientAwareProperties.class,
                allProperties);
        
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

        /*
         * build and configure the REST client
         */

        final ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(client.getBaseUrl());

        configureFeignBuilder(
                DriverCallbackApi.class,
                apiClient.getFeignBuilder(),
                client);
        
        return apiClient.buildClient(DriverCallbackApi.class);
        
    }

}
