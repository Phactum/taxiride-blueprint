package com.taxicompany.ride.service;

import com.taxicompany.driver.client.v1.DriverServiceApi;
import com.taxicompany.driver.client.v1.DriversNearbyParameters;
import com.taxicompany.driver.client.v1.RequestRideOfferParameters;
import com.taxicompany.ride.domain.Location;
import com.taxicompany.ride.domain.Ride;
import com.taxicompany.ride.domain.RideRepository;
import io.vanillabp.spi.process.ProcessService;
import io.vanillabp.spi.service.BpmnProcess;
import io.vanillabp.spi.service.MultiInstanceElement;
import io.vanillabp.spi.service.MultiInstanceIndex;
import io.vanillabp.spi.service.WorkflowService;
import io.vanillabp.spi.service.WorkflowTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;

@Service
@WorkflowService(
        workflowAggregateClass = Ride.class,
        bpmnProcess = @BpmnProcess(bpmnProcessId = "TaxiRide"))
@Transactional
public class DetermineDriver {
    
    public static final float MAX_SCORE = 10f;

    private static final Logger logger = LoggerFactory.getLogger(DetermineDriver.class);
    
    @Autowired
    private ProcessService<Ride> processService;

    @Autowired
    private DriverServiceApi driverService;

    @Autowired
    private DriverServiceMapper mapper;
    
    @Autowired
    private RideRepository rides;

    @WorkflowTask
    public void determinePotentialDrivers(
            final Ride ride) {
        
        final var parameters = new DriversNearbyParameters()
                .longitude(ride.getPickupLocation().getLongitude())
                .latitude(ride.getPickupLocation().getLatitude());

        final var potentialDrivers = driverService.determineDriversNearby(parameters);

        ride.setPotentialDrivers(mapper.toDomain(potentialDrivers, ride));

    }
    
    @WorkflowTask
    public void requestRideOfferFromDriver(
            final Ride ride,
            @MultiInstanceIndex("RequestRideOffer")
            final int potentialDriverIndex) {
        
        final var driver = ride.getPotentialDrivers().get(potentialDriverIndex);
        
        driverService.requestRideOffer(
                driver.getId(),
                new RequestRideOfferParameters()
                        .rideId(ride.getRideId())
                        .pickupLocation(mapper.toApi(ride.getPickupLocation()))
                        .pickupTime(ride.getPickupTime())
                        .targetLocation(mapper.toApi(ride.getTargetLocation())));
        
    }
    
    public void offerReceived(
            final String rideId,
            final String driverId,
            final Location currentDriverLocation,
            final int passengersUntilPickup,
            final OffsetDateTime pickupTime) {

        final var ride = rides
                .findById(rideId)
                .orElseThrow();
        
        final var driver = ride
                .getPotentialDrivers()
                .stream()
                .filter(current -> current.getId().equals(driverId))
                .findFirst()
                .orElseThrow();
        
        driver.setCurrentLocation(currentDriverLocation);
        driver.setPassengersUntilPickup(passengersUntilPickup);
        driver.setPickupTime(pickupTime);
        
        processService.correlateMessage(
                ride,
                "RideOfferReceived",
                rideId + "-" + driverId);

    }
    
    @WorkflowTask
    public void scoreOfferOfDriver(
            final Ride ride,
            @MultiInstanceIndex("RequestRideOffer")
            final int potentialDriverIndex) {
        
        final var driver = ride.getPotentialDrivers().get(potentialDriverIndex);
        
        final var distance = distance(
                driver.getCurrentLocation().getLatitude(),
                driver.getCurrentLocation().getLongitude(),
                ride.getPickupLocation().getLatitude(),
                ride.getPickupLocation().getLongitude(),
                "K");
        
        final var minutesLate = Duration
                .between(ride.getPickupTime(), driver.getPickupTime())
                .toMinutes();
        
        final var score
                = ((MAX_SCORE - distance) 
                + (MAX_SCORE - driver.getPassengersUntilPickup() * 3 /* boost */)
                + (MAX_SCORE - minutesLate < 0 ? 0 : minutesLate))
                / 3 /* limit to MAX_SCORE by dividing according to the number of aspects added */;
        
        logger.info("driver-scoring {}: distance {}km, passangers until pickup {}, minutes late {} -> {}",
                driver.getName(),
                distance,
                driver.getPassengersUntilPickup(),
                minutesLate,
                score);
        
        driver.setScore((float) score);
        
    }
    
    @WorkflowTask
    public void selectDriverAccordingToScore(
            final Ride ride) {
        
        final var winner = ride
                .getPotentialDrivers()
                .stream()
                .filter(driver -> driver.getScore() != null)
                .sorted((a, b) -> a.getScore() == b.getScore() ? 0 : a.getScore() < b.getScore() ? 1 : -1)
                .findFirst();
        
        if (winner.isEmpty()) {
            logger.warn("driver-scoring for ride {}: none", ride.getRideId());
            return;
        }
            
        logger.info("driver-scoring for ride {}: {}", ride.getRideId(), winner.get().getName());
        ride.setDriver(winner.get());

    }
    
    @WorkflowTask
    public void cancelRideOfferOfDriver(
            final Ride ride,
            @MultiInstanceElement("CancelNotRequiredRide")
            final String driverIdOfUnselectedOffer) {
        
        final var driver = ride
                .getPotentialDrivers()
                .stream()
                .filter(candidate -> candidate.getId().equals(driverIdOfUnselectedOffer))
                .findFirst()
                .orElseThrow();
        
        driverService.cancelRideOffer(
                driver.getId(),
                ride.getRideId());

    }

    /**
     * @see https://www.geodatasource.com/developers/java
     */
    private static double distance(
            final double lat1,
            final double lon1,
            final double lat2,
            final double lon2,
            final String unit) {
        
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        }
        else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            if (unit.equals("K")) {
                dist = dist * 1.609344;
            } else if (unit.equals("N")) {
                dist = dist * 0.8684;
            }
            return (dist);
        }
        
    }
    
}
