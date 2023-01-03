package com.taxicompany;

import io.vanillabp.springboot.ModuleAndWorkerAwareSpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackageClasses = { TaxiApplication.class })
@EnableScheduling
public class TaxiApplication {

    public static void main(String... args) {

        System.setProperty("hibernate.types.print.banner", "false");

        new ModuleAndWorkerAwareSpringApplication(TaxiApplication.class).run(args);

    }

}
