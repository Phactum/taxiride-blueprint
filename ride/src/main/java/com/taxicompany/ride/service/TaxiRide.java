package com.taxicompany.ride.service;

import com.taxicompany.driver.client.v1.DriverServiceApi;
import com.taxicompany.ride.config.RideProperties;
import com.taxicompany.ride.domain.Location;
import com.taxicompany.ride.domain.Ride;
import com.taxicompany.ride.domain.RideRepository;
import io.vanillabp.spi.process.ProcessService;
import io.vanillabp.spi.service.MultiInstanceElement;
import io.vanillabp.spi.service.TaskEvent;
import io.vanillabp.spi.service.TaskEvent.Event;
import io.vanillabp.spi.service.TaskException;
import io.vanillabp.spi.service.TaskId;
import io.vanillabp.spi.service.WorkflowService;
import io.vanillabp.spi.service.WorkflowTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
@WorkflowService(workflowAggregateClass = Ride.class)
@Transactional(noRollbackFor = TaskException.class)
public class TaxiRide {
    
    @Autowired
    private ProcessService<Ride> processService;
    
    @Autowired
    private RideProperties properties;
    
    @Autowired
    private DriverServiceApi driverService;
    
    @Autowired
    private RideRepository rides;

    public String rideBooked(
            final Location pickupLocation,
            final OffsetDateTime pickupTime,
            final Location targetLocation) {
        
        final var ride = new Ride();
        ride.setPickupLocation(pickupLocation);
        ride.setPickupTime(pickupTime);
        ride.setTargetLocation(targetLocation);
        ride.setOfferingDeadline(
                determineOfferingDeadline(pickupTime));
        
        return processService
                .correlateMessage(ride, "RideBooked")
                .getRideId();
        
    }
    
    private Date determineOfferingDeadline(
            final OffsetDateTime pickupTime) {
        
        if (pickupTime.isBefore(
                OffsetDateTime.now().plus(properties.getPeriodForImmediatelyPickups()))) {
            
            // if pickup time is within configured threshold immediate rides
            // then wait 5 minutes for ride offers
            return Date.from(
                    OffsetDateTime
                            .now()
                            .plus(5, ChronoUnit.MINUTES)
                            .toInstant());
            
        }
        
        // if pickup time is not within configured threshold immediate rides
        // then wait 10 minutes less then threshold duration
        return Date.from(
                OffsetDateTime
                        .now()
                        .plus(properties.getPeriodForImmediatelyPickups())
                        .minus(10, ChronoUnit.MINUTES)
                        .toInstant());
        
    }
    
    @WorkflowTask
    public void confirmRideToDriver(
            final Ride ride) {
        
        driverService.confirmRideOffer(
                ride.getDriver().getId(),
                ride.getRideId(),
                "Confirmed");
        
    }
    
    @WorkflowTask
    public void cancelRideOfferOfDriverOnAbort(
            final Ride ride,
            @MultiInstanceElement("CancelNotRequiredRideOnAbort")
            final String driverIdOfOffer) {
        
        final var driver = ride
                .getPotentialDrivers()
                .stream()
                .filter(candidate -> candidate.getId().equals(driverIdOfOffer))
                .findFirst()
                .orElseThrow();
        
        driverService.cancelRideOffer(
                driver.getId(),
                ride.getRideId());
        
    }
    
    @WorkflowTask
    public void retrievePaymentFromDriver(
            final Ride ride) {
        
        final var outstandingAmount = ride.getPrice() - ride.getCharged();

        driverService.retrievePayment(
                ride.getDriver().getId(),
                ride.getRideId(),
                outstandingAmount);
                
    }

    public void rideFinished(
            final String rideId,
            final String driverId,
            final float price,
            final float charged) {
        
        final var ride = rides
                .findById(rideId)
                .orElseThrow();
        
        if ((ride.getDriver() == null)
                || !ride.getDriver().getId().equals(driverId)) {
            
            throw new UnsupportedOperationException();
            
        }
        
        ride.setPrice(price);
        ride.setCharged(charged);
        
        processService.correlateMessage(
                ride,
                "RideFinished",
                ride.getRideId());
        
    }
    
    @WorkflowTask
    public void payDriverFee(
            final Ride ride) {
        
        final var outstandingAmount = ride.getPrice() - ride.getCharged();

        /* charge from payment-service-provider */

        driverService.feePayed(
                ride.getDriver().getId(),
                ride.getRideId(),
                outstandingAmount);
        
    }
    
    @WorkflowTask
    public void chargeRide(
            final Ride ride) {
        
        /* charge from payment-service-provider */

        throw new TaskException("CreditCardCannotCharged");

    }
    
    @WorkflowTask
    public void retrievePayment(
            final Ride ride,
            final @TaskId String taskId,
            final @TaskEvent Event taskEvent) {
        
        if (taskEvent == Event.CREATED) {
            ride.setRetrievePaymentTaskId(taskId);
        } else {
            ride.setRetrievePaymentTaskId(null);
        }
        
    }

    public void paymentRetrieved(
            final String rideId,
            final float charged) {
        
        final var ride = rides
                .findById(rideId)
                .orElseThrow();
        
        ride.setCharged(charged);
        
        processService.completeUserTask(
                ride,
                ride.getRetrievePaymentTaskId());

    }

}
