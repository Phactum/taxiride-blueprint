package com.taxicompany.simulation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import com.taxicompany.simulation.config.ApplicationProperties;

@SpringBootApplication
@ComponentScan(basePackages = "com.taxicompany")
@EnableConfigurationProperties(ApplicationProperties.class)
public class TaxiSimulation {

    public static void main(String... args) {

        new SpringApplication(TaxiSimulation.class).run(args);

    }

}
