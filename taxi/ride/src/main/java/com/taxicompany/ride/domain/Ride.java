package com.taxicompany.ride.domain;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
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

    @Column(name = "PICKUP_TIME", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime pickupTime;

    @Type(type = "json")
    @Column(name = "TARGET_LOCATION", nullable = false, columnDefinition = "JSON")
    private Location targetLocation;

    @OneToMany(mappedBy = "ride", fetch = FetchType.LAZY, cascade = { CascadeType.ALL })
    private List<Driver> potentialDrivers;

    @Column(name = "OFFERING_DEADLINE", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Date offeringDeadline;

    @OneToOne
    @JoinColumn(name = "DRIVER")
    private Driver driver;

    @Column(name = "PRICE")
    private Float price;

    @Column(name = "CHARGED")
    private Float charged;

    public boolean isCustomerCharged() {

        return price == charged;

    }

    public boolean isNoRideAvailable() {

        return driver == null;

    }
    
    public Collection<String> getUnselectedOffers() {
        
        return potentialDrivers
                .stream()
                .map(Driver::getId)
                .filter(candidate -> isNoRideAvailable() || !candidate.equals(driver.getId()))
                .collect(Collectors.toList());
        
    }

    public Collection<String> getAllOffers() {
        
        return potentialDrivers
                .stream()
                .map(Driver::getId)
                .collect(Collectors.toList());
        
    }

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

    public Date getOfferingDeadline() {
        return offeringDeadline;
    }

    public void setOfferingDeadline(Date offeringDeadline) {
        this.offeringDeadline = offeringDeadline;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public Float getCharged() {
        return charged;
    }

    public void setCharged(Float charged) {
        this.charged = charged;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public Float getPrice() {
        return price;
    }

}
