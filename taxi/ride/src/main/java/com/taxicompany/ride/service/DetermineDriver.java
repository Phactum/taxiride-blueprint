package com.taxicompany.ride.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taxicompany.driver.client.v1.DriverServiceApi;
import com.taxicompany.driver.client.v1.DriversNearbyParameters;
import com.taxicompany.ride.domain.Ride;

import at.phactum.bp.blueprint.service.BpmnProcess;
import at.phactum.bp.blueprint.service.MultiInstanceElement;
import at.phactum.bp.blueprint.service.WorkflowService;
import at.phactum.bp.blueprint.service.WorkflowTask;

@Service
@WorkflowService(
        workflowAggregateClass = Ride.class,
        bpmnProcess = @BpmnProcess(bpmnProcessId = "TaxiRide"))
public class DetermineDriver {
    
    @Autowired
    private DriverServiceApi driverService;

    @Autowired
    private DriverServiceMapper mapper;

    @WorkflowTask
    public void determinePotentialDrivers(
            final Ride ride) {
        
        final var parameters = new DriversNearbyParameters();
        parameters.setLatitude(ride.getPickupLocation().getLongitude());
        parameters.setLatitude(ride.getPickupLocation().getLatitude());

        final var potentialDrivers = driverService.determineDriversNearby(parameters);

        ride.setPotentialDrivers(mapper.toDomain(potentialDrivers));

    }
    
    @WorkflowTask
    public void scoreOfferOfDriver(
            final Ride ride,
            @MultiInstanceElement("RequestRideOffer")
            final String potentialDriverId) {
        
    }
    
    @WorkflowTask
    public void requestRideOfferFromDriver(
            final Ride ride,
            @MultiInstanceElement("RequestRideOffer")
            final String potentialDriverId) {
        
    }
    
}
