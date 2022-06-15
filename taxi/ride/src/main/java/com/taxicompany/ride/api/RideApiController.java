package com.taxicompany.ride.api;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taxicompany.ride.api.v1.RideApi;
import com.taxicompany.ride.api.v1.RideBookedParameters;
import com.taxicompany.ride.api.v1.RideCharged;
import com.taxicompany.ride.service.TaxiRide;

@RestController
@RequestMapping("/api/v1")
public class RideApiController implements RideApi {

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
    
    @Override
    public ResponseEntity<String> rideCharged(
            final String rideId,
            final @Valid RideCharged rideCharged) {
        
        taxiRide.paymentRetrieved(rideId, rideCharged.getAmount());
        
        return ResponseEntity.ok(rideId);

    }
    
}
