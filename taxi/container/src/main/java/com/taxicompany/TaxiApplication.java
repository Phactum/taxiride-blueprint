package com.taxicompany;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import at.phactum.bp.blueprint.modules.ModuleAndWorkerAwareSpringApplication;

@SpringBootApplication
@ComponentScan(basePackageClasses = { TaxiApplication.class })
public class TaxiApplication {

    public static void main(String... args) {

        System.setProperty("hibernate.types.print.banner", "false");

        new ModuleAndWorkerAwareSpringApplication(TaxiApplication.class).run(args);

    }

}
