package org.blueprint.bp.blueprint;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import at.phactum.bp.blueprint.modules.ModuleAndWorkerAwareSpringApplication;

@SpringBootApplication
@ComponentScan(basePackageClasses = { BlueprintApplication.class })
public class BlueprintApplication {

    public static void main(String... args) {

        new ModuleAndWorkerAwareSpringApplication(BlueprintApplication.class).run(args);

    }

}
