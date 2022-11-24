package com.taxicompany;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import at.phactum.bp.blueprint.modules.ModuleAndWorkerAwareSpringApplication;
import com.taxicompany.config.ApplicationProperties;

@SpringBootApplication
@ComponentScan(basePackageClasses = { TaxiApplication.class })
@EnableConfigurationProperties(ApplicationProperties.class)
public class TaxiApplication {

    public static void main(String... args) {

        System.setProperty("hibernate.types.print.banner", "false");

        new ModuleAndWorkerAwareSpringApplication(TaxiApplication.class).run(args);

    }

}
