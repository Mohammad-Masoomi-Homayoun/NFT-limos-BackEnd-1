package com.niftylimos.web;

import com.niftylimos.service.ReserveTrackerService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reserve/")
public class ReserveTrackerController {

    private final ReserveTrackerService service;

    public ReserveTrackerController(ReserveTrackerService service) {
        this.service = service;
    }

    @RequestMapping(method = RequestMethod.GET, value = "total")
    public int getNumTotalReserved() {
        return service.getNumTotalReserved();
    }

    @RequestMapping(method = RequestMethod.GET, value = "account/{acc}")
    public int getNumAccountReserved(@PathVariable String acc) {
        return service.getNumAccountReserved(acc);
    }
}
