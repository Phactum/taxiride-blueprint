package com.taxicompany.simulation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.taxicompany.simulation.config.ApplicationProperties;

@SpringBootApplication
@ComponentScan(basePackages = "com.taxicompany")
@EnableConfigurationProperties(ApplicationProperties.class)
@EnableAsync
@EnableScheduling
public class TaxiSimulation {

    public static void main(String... args) {

        new SpringApplication(TaxiSimulation.class).run(args);

    }

}
