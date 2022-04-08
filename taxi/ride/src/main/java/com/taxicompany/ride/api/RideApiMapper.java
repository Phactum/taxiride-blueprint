package com.taxicompany.ride.api;

import org.mapstruct.Mapper;

import com.taxicompany.ride.api.v1.Location;

@Mapper
public interface RideApiMapper {

    com.taxicompany.ride.domain.Location toDomain(Location location);

}
