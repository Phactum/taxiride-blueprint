package com.taxicompany.simulation.driver;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taxicompany.driver.service.v1.Driver;
import com.taxicompany.driver.service.v1.DriverApi;
import com.taxicompany.driver.service.v1.DriversNearbyParameters;
import com.taxicompany.driver.service.v1.RequestRideOfferParameters;

@RestController
@RequestMapping("/api/v1")
public class DriverApiController implements DriverApi {

    private static final List<Driver> DRIVERS = new ArrayList<>();

    static {

        for (int i = 0; i < 4; ++i) {

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

            driver.setName(name);

            DRIVERS.add(driver);

        }

    }

    private Random random = new Random(System.currentTimeMillis());

    @Autowired
    private OfferService offerService;

    @Autowired
    private TaskScheduler taskScheduler;

    @Override
    public ResponseEntity<List<Driver>> determineDriversNearby(final DriversNearbyParameters driversNearbyParameters) {

        final var result = new LinkedList<Driver>();

        final var noOfDrivers = random.nextInt(4) + 1;
        for (int i = 0; i < noOfDrivers; ++i) {

            final var driver = DRIVERS.get(i);
            result.add(driver);

        }

        return ResponseEntity.ok(result);

    }
    
    @Override
    public ResponseEntity<Void> requestRideOffer(
            final String driverId,
            final @Valid RequestRideOfferParameters request) {
        
        taskScheduler.schedule(
                () -> offerService.requestRideOffer(driverId, request),
                Date.from(LocalDateTime
                    .now()
                    .plus(random.nextInt(3), ChronoUnit.SECONDS)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()));

        return ResponseEntity.ok().build();
        
    }
    
    @Override
    public ResponseEntity<Void> cancelRideOffer(
            final String driverId,
            final String rideId) {
        
        /* nothing to do in simulation */
        
        return ResponseEntity.ok().build();
        
    }
    
    @Override
    public ResponseEntity<Void> confirmRideOffer(
            final String driverId,
            final String rideId,
            final @Valid String toBeIgnored) {
        
        taskScheduler.schedule(
                () -> offerService.finishRide(driverId, rideId),
                Date.from(LocalDateTime
                    .now()
                    .plus(random.nextInt(5), ChronoUnit.SECONDS)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()));
        
        return ResponseEntity.ok().build();
        
    }
    
    @Override
    public ResponseEntity<Void> retrievePayment(
            final String driverId,
            final String rideId,
            final @Valid Float amount) {
        
        /* nothing to do */
        
        return ResponseEntity.ok().build();
        
    }
    
    @Override
    public ResponseEntity<Void> feePayed(
            final String driverId,
            final String rideId,
            final @Valid Float amount) {

        /* nothing to do */

        return ResponseEntity.ok().build();

    }

    public static Driver getDriver(
            final String driverId) {
        
        return DRIVERS
                .stream()
                .filter(driver -> driver.getId().equals(driverId))
                .findFirst()
                .orElseThrow();
        
    }

}
