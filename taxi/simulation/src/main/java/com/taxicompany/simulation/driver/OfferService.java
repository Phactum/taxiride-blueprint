package com.taxicompany.simulation.driver;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.taxicompany.driver.callback.client.v1.DriverCallbackServiceClientAwareProperties;
import com.taxicompany.driver.callback.v1.DriverCallbackApi;
import com.taxicompany.driver.callback.v1.Location;
import com.taxicompany.driver.callback.v1.RideFinished;
import com.taxicompany.driver.callback.v1.RideOffer;
import com.taxicompany.driver.service.v1.RequestRideOfferParameters;

@Service
public class OfferService {

    private Random random = new Random(System.currentTimeMillis());

    @Autowired
    private DriverCallbackApi driverCallbackApi;

    @SuppressWarnings("unused")
    @Autowired
    private DriverCallbackServiceClientAwareProperties properties;

    @Async
    public void finishRide(
            final String driverId,
            final String rideId) {
        
        final var payment = new RideFinished();
        payment.setPrice(random.nextFloat() * 40);
        payment.setCharged(random.nextBoolean() ? payment.getPrice() : 0);
        
        driverCallbackApi
                .rideFinished(driverId, rideId, payment);
        
    }
    
    @Async
    public void requestRideOffer(
            final String driverId,
            final RequestRideOfferParameters request) {
        
        final var offer = new RideOffer();

        final var driver = DriverApiController.getDriver(driverId);
        offer.setDriverName(driver.getName());
        
        // simulate outstanding passengers
        offer.setPassengersUntilPickup(random.nextInt(3));
        if (request.getPickupTime().isBefore(
                OffsetDateTime.now().plus(30, ChronoUnit.MINUTES))) {
            
            offer.setPickupTime(
                    OffsetDateTime
                            .now()
                            .plus(offer.getPassengersUntilPickup() * 10, ChronoUnit.MINUTES));
            
        } else {
            
            offer.setPickupTime(request.getPickupTime());

        }
        
        // simulate "nearby"
        final var pickupLocation = request.getPickupLocation();
        offer.setCurrentDriverLocation(
                new Location()
                        .longitude(pickupLocation.getLongitude()
                                + (0.2 * random.nextDouble()) - 0.1)
                        .latitude(pickupLocation.getLatitude()
                                + (0.2 * random.nextDouble()) - 0.1));
        
        driverCallbackApi.rideOffer(
                driverId,
                request.getRideId(),
                offer);
        
    }
    
}
