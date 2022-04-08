package com.taxicompany.ride.service;

import java.util.List;

import org.mapstruct.Mapper;

import com.taxicompany.driver.client.v1.Driver;

@Mapper
public interface DriverServiceMapper {

    com.taxicompany.ride.domain.Driver toDomain(Driver driver);

    List<com.taxicompany.ride.domain.Driver> toDomain(List<Driver> driver);

}
