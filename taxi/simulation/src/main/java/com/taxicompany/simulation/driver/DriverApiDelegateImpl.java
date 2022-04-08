package com.taxicompany.simulation.driver;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.taxicompany.driver.service.v1.Driver;
import com.taxicompany.driver.service.v1.DriverApiDelegate;
import com.taxicompany.driver.service.v1.DriversNearbyParameters;

@Component
public class DriverApiDelegateImpl implements DriverApiDelegate {

    private Random random = new Random(System.currentTimeMillis());

    @Override
    public ResponseEntity<List<Driver>> determineDriversNearby(
            final DriversNearbyParameters driversNearbyParameters) {

        final var result = new LinkedList<Driver>();

        final var noOfDrivers = random.nextInt(4) + 1;
        for (int i = 0; i < noOfDrivers; ++i) {

            final var driver = new Driver();
            driver.setId(UUID.randomUUID().toString());

            String name;
            switch (i) {
            case 1:
                name = "Peter";
                break;
            case 2:
                name = "Lisa";
                break;
            case 3:
                name = "Sebastian";
                break;
            default:
                name = "Sarah";
            }
            ;
            driver.setName(name);

            result.add(driver);

        }

        return ResponseEntity.ok(result);

    }

}
