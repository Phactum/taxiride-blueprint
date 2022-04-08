package com.taxicompany.simulation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.taxicompany")
public class TaxiSimulation {

    public static void main(String... args) {

        new SpringApplication(TaxiSimulation.class).run(args);

    }

}
