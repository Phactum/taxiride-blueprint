package com.taxicompany.config;

import at.phactum.bp.blueprint.async.AsyncProperties;
import at.phactum.bp.blueprint.async.AsyncPropertiesAware;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "taxi")
public class ApplicationProperties implements AsyncPropertiesAware {

    private AsyncProperties async = new AsyncProperties();
    
    @Override
    public AsyncProperties getAsync() {
        return async;
    }
    
    public void setAsync(AsyncProperties async) {
        this.async = async;
    }
    
}
