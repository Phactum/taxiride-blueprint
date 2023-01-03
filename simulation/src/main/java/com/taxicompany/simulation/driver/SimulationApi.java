package com.taxicompany.simulation.driver;

import javax.websocket.server.PathParam;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class SimulationApi {

    @GetMapping(path = "/ride/{rideId}/offer/{offerId}/driver/{driverId}")
    public void triggerOffer(
            @PathParam("rideId") final String rideId,
            @PathParam("driverId") final String driverId,
            @PathParam("offerId") final String offerId) {
        
        
        
    }
    
}
