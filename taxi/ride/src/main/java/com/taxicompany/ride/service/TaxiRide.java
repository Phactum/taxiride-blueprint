package com.taxicompany.ride.service;

import java.time.OffsetDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taxicompany.ride.domain.Location;
import com.taxicompany.ride.domain.Ride;

import at.phactum.bp.blueprint.process.ProcessService;
import at.phactum.bp.blueprint.service.MultiInstanceElement;
import at.phactum.bp.blueprint.service.WorkflowService;
import at.phactum.bp.blueprint.service.WorkflowTask;

@Service
@WorkflowService(workflowAggregateClass = Ride.class)
public class TaxiRide {
    
    @Autowired
    private ProcessService<Ride> processService;
    
    public String rideBooked(
            final Location pickupLocation,
            final OffsetDateTime pickupTime,
            final Location targetLocation) {
        
        final var ride = new Ride();
        ride.setPickupLocation(pickupLocation);
        ride.setPickupTime(pickupTime);
        ride.setTargetLocation(targetLocation);
        
        return processService
                .correlateMessage(ride, "RideBooked")
                .getRideId();
        
    }
    
    @WorkflowTask
    public void confirmRideToDriver(
            final Ride ride) {
        
    }
    
    @WorkflowTask
    public void cancelRideOfferOfDriver(
            final Ride ride,
            @MultiInstanceElement("CancelNotRequiredRide")
            final String unselectedOfferId) {
        
    }
    
    @WorkflowTask
    public void cancelRideOfferOfDriverOnAbort(
            final Ride ride,
            @MultiInstanceElement("CancelNotRequiredRideOnAbort")
            final String offerId) {
        
    }

    @WorkflowTask
    public void payDriverFee(
            final Ride ride) {
        
    }
    
    @WorkflowTask
    public void chargeRide(
            final Ride ride) {
        
    }
    
}
