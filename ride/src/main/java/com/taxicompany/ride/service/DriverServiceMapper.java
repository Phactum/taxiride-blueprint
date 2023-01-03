package com.taxicompany.ride.service;

import java.util.List;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.taxicompany.driver.client.v1.Driver;
import com.taxicompany.driver.client.v1.Location;
import com.taxicompany.ride.domain.Ride;

@Mapper
public interface DriverServiceMapper {

    @Mapping(target = "currentLocation", ignore = true)
    @Mapping(target = "pickupTime", ignore = true)
    @Mapping(target = "passengersUntilPickup", ignore = true)
    @Mapping(target = "score", ignore = true)
    @Mapping(target = "ride", expression = "java(ride)")
    com.taxicompany.ride.domain.Driver toDomain(Driver driver, @Context Ride ride);

    List<com.taxicompany.ride.domain.Driver> toDomain(List<Driver> driver, @Context Ride ride);

    com.taxicompany.ride.domain.Location toDomain(Location location);

    Location toApi(com.taxicompany.ride.domain.Location location);

}
