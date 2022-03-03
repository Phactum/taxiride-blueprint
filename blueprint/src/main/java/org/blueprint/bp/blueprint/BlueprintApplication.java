package org.blueprint.bp.blueprint;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import at.phactum.bp.blueprint.modules.ModuleAndWorkerAwareSpringApplication;
import io.camunda.zeebe.spring.client.EnableZeebeClient;

@SpringBootApplication
@ComponentScan(basePackageClasses = BlueprintApplication.class)
@EnableZeebeClient
public class BlueprintApplication {

    @SuppressWarnings("resource")
    public static void main(String... args) {

        new ModuleAndWorkerAwareSpringApplication(BlueprintApplication.class).run(args);

    }

}
