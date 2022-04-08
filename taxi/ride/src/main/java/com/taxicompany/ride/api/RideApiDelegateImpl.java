package com.taxicompany.ride.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.taxicompany.ride.api.v1.RideApiDelegate;
import com.taxicompany.ride.api.v1.RideBookedParameters;
import com.taxicompany.ride.service.TaxiRide;

@Component
public class RideApiDelegateImpl implements RideApiDelegate {

    @Autowired
    private RideApiMapper mapper;
    
    @Autowired
    private TaxiRide taxiRide;
    
    @Override
    public ResponseEntity<String> rideBooked(
            final RideBookedParameters rideBookedParameters) {
        
        final var rideId = taxiRide.rideBooked(
                mapper.toDomain(rideBookedParameters.getPickupLocation()),
                rideBookedParameters.getPickupTime(),
                mapper.toDomain(rideBookedParameters.getTargetLocation()));
        
        return ResponseEntity.ok(rideId);
        
    }
    
}
