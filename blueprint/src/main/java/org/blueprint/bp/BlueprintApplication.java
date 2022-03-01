package org.blueprint.bp;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import at.phactum.bp.blueprint.modules.ModuleAndWorkerAwareSpringApplication;

@SpringBootApplication
@ComponentScan(basePackageClasses = BlueprintApplication.class)
public class BlueprintApplication {

    @SuppressWarnings("resource")
    public static void main(String... args) {

        new ModuleAndWorkerAwareSpringApplication(BlueprintApplication.class).run(args);

    }

}
