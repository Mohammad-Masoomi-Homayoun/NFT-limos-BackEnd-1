package com.niftylimos.web;

import com.niftylimos.service.ReserveStatus;
import com.niftylimos.service.ReserveTrackerService;
import org.springframework.web.bind.annotation.GetMapping;
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

    @RequestMapping(method = RequestMethod.GET, value = "status")
    public ReserveStatus get(){
        return service.getStatus();
    }
}
