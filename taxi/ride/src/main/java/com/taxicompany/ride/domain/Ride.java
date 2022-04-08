package com.taxicompany.ride.domain;

import java.time.OffsetDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "RIDES")
public class Ride {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "ID")
    private String rideId;

    @Type(type = "json")
    @Column(name = "PICKUP_LOCATION", nullable = false, columnDefinition = "JSON")
    private Location pickupLocation;

    @Column(name = "PICKUPT_TIME", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime pickupTime;

    @Type(type = "json")
    @Column(name = "TARGET_LOCATION", nullable = false, columnDefinition = "JSON")
    private Location targetLocation;

    @Type(type = "json")
    @Column(name = "POTENTIAL_DRIVERS", columnDefinition = "JSON")
    private List<Driver> potentialDrivers;

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public Location getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(Location pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public OffsetDateTime getPickupTime() {
        return pickupTime;
    }

    public void setPickupTime(OffsetDateTime pickupTime) {
        this.pickupTime = pickupTime;
    }

    public Location getTargetLocation() {
        return targetLocation;
    }

    public void setTargetLocation(Location targetLocation) {
        this.targetLocation = targetLocation;
    }

    public List<Driver> getPotentialDrivers() {
        return potentialDrivers;
    }

    public void setPotentialDrivers(List<Driver> potentialDrivers) {
        this.potentialDrivers = potentialDrivers;
    }

}
