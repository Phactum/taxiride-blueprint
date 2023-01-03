package com.taxicompany.ride.api;

import org.mapstruct.Mapper;

import com.taxicompany.driver.service.v1.Location;

@Mapper
public interface DriverCallbackApiMapper {

    com.taxicompany.ride.domain.Location toDomain(Location location);

}
