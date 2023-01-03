package com.taxicompany.driver.client.v1;

import at.phactum.bp.blueprint.rest.adapter.ClientsConfigurationBase;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.util.ClassUtils;

import java.util.List;

/**
 * Builds a driver service REST-client for each injection point based on the
 * workflow module's properties to configure attributes like URL, logging,
 * authentication, etc.
 * 
 * @see DriverServiceClientAwareProperties
 */
@Configuration
public class ClientsConfiguration extends ClientsConfigurationBase {

    /**
     * Properties beans used by various workflow modules.
     * 
     * @see {@link ClientsConfigurationBase#getProperties(InjectionPoint, Class, List)}
     */
    @Autowired
    private List<DriverServiceClientAwareProperties> allProperties;

    /**
     * Build a driver service REST client. If this is autowired by different
     * workflow modules then multiple client beans are built using the respective
     * workflow module's properties bean for configuration (which has to implement
     * {@link DriverServiceClientAwareProperties}).
     * 
     * @param injectionPoint The injection
     * @return The REST client
     */
    @Bean
    @Scope("prototype")
    public DriverServiceApi driverService(
            final InjectionPoint injectionPoint) {
        
        /*
         * Find the right properties in workflow module of the given injection point
         */

        final var properties = getProperties(
                injectionPoint,
                DriverServiceClientAwareProperties.class,
                allProperties);
        
        final var client = properties.getDriverServiceClient();
        if (client == null) {
            final var propertiesClass = determineBeanClass(properties);
            final var annotation = propertiesClass.getAnnotation(ConfigurationProperties.class);
            throw new RuntimeException(
                    "No section '"
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
                DriverServiceApi.class,
                apiClient.getFeignBuilder(),
                client);
        
        return apiClient.buildClient(DriverServiceApi.class);
        
    }

    private Class<?> determineBeanClass(
            final Object bean) {
        
        final var proxyClass = bean.getClass();
        final var result = AopUtils.getTargetClass(bean);
        if (result != proxyClass) {
            return result;
        }
        return ClassUtils.getUserClass(bean);
        
    }

    
}
