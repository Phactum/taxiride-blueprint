package at.phactum.bp.blueprint.camunda8.adapter.test.testcase;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import at.phactum.bp.blueprint.bpm.deployment.BpDeploymentConfiguration;
import at.phactum.bp.blueprint.modules.ModuleSpecificProperties;

@Configuration
@ConfigurationProperties(prefix = "test")
public class TestModuleProperties implements BpDeploymentConfiguration {

    public static final String WORKFLOW_MODULE_ID = "test";

    private String processesLocation = "test-processes";

    private boolean myConfig;

    @Bean
    public static ModuleSpecificProperties moduleProps() {

        return new ModuleSpecificProperties(TestModuleProperties.class, "test");

    }

    @Override
    public String getProcessesLocation() {
        return processesLocation;
    }

    public void setProcessesLocation(String processesLocation) {
        this.processesLocation = processesLocation;
    }

    public boolean isMyConfig() {
        return myConfig;
    }

    public void setMyConfig(boolean myConfig) {
        this.myConfig = myConfig;
    }

}
