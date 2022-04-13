package com.taxicompany.ride.api;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taxicompany.driver.service.v1.DriverCallbackApi;
import com.taxicompany.driver.service.v1.RideFinished;
import com.taxicompany.driver.service.v1.RideOffer;
import com.taxicompany.ride.service.DetermineDriver;
import com.taxicompany.ride.service.TaxiRide;

@RestController
@RequestMapping(path = "/api/v1")
public class DriverCallbackApiController implements DriverCallbackApi {

    @Autowired
    private DetermineDriver determineDriver;
    
    @Autowired
    private TaxiRide taxiRide;
    
    @Autowired
    private DriverCallbackApiMapper mapper;
    
    @Override
    public ResponseEntity<Void> rideOffer(
            final String driverId,
            final String rideId,
            final @Valid RideOffer rideOffer) {

        determineDriver.offerReceived(
                rideId,
                driverId,
                mapper.toDomain(rideOffer.getCurrentDriverLocation()),
                rideOffer.getPassengersUntilPickup(),
                rideOffer.getPickupTime());
        
        return ResponseEntity.ok().build();
        
    }
    
    @Override
    public ResponseEntity<Void> rideFinished(
            final String driverId,
            final String rideId,
            @Valid RideFinished rideFinished) {
        
        taxiRide.rideFinished(
                rideId,
                driverId,
                rideFinished.getPrice(),
                rideFinished.getCharged());

        return ResponseEntity.ok().build();
        
    }
    
}
