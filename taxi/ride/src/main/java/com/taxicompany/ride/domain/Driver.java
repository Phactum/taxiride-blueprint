package com.taxicompany.ride.domain;

import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "POTENTIAL_DRIVERS")
public class Driver {

    @Id
    @Column(name = "ID")
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "RIDE", nullable = false, updatable = false)
    private Ride ride;

    @Column(name = "NAME")
    private String name;

    @Type(type = "json")
    @Column(name = "PICKUP_LOCATION", columnDefinition = "JSON")
    private Location currentLocation;

    @Column(name = "PICKUP_TIME", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime pickupTime;

    @Column(name = "REMAINING_PASSENGERS")
    private int passengersUntilPickup;

    @Column(name = "SCORE")
    private Float score;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public OffsetDateTime getPickupTime() {
        return pickupTime;
    }

    public void setPickupTime(OffsetDateTime pickupTime) {
        this.pickupTime = pickupTime;
    }

    public int getPassengersUntilPickup() {
        return passengersUntilPickup;
    }

    public void setPassengersUntilPickup(int passengersUntilPickup) {
        this.passengersUntilPickup = passengersUntilPickup;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public Ride getRide() {
        return ride;
    }

    public void setRide(Ride ride) {
        this.ride = ride;
    }

}
