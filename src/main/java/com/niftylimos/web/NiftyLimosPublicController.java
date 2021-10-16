package com.niftylimos.web;

import com.niftylimos.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/")
public class NiftyLimosPublicController {

    private static final Logger logger = LoggerFactory.getLogger(NiftyLimosPublicController.class);

    private final NiftyLimosService service;

    public NiftyLimosPublicController(NiftyLimosService service) {
        this.service = service;
    }

    @RequestMapping(value = "/account")
    public List<AccountDTO> getAllAccounts() {
        return service.getAllAccounts();
    }

    @RequestMapping(value = "/account/{id}")
    public AccountDTO getAccount(@PathVariable String id) {
        return service.getAccount(id);
    }

    @RequestMapping(value = "/reservation")
    public List<ReservationDTO> getAllReservation() {
        return service.getAllReservations();
    }

    @RequestMapping(value = "/reservation/{id}")
    public ReservationDTO getReservation(@PathVariable Long id) {
        return service.getReservation(id);
    }

    @RequestMapping(value = "/limo/{id}")
    public LimoDTO getLimo(@PathVariable Long id) {
        return service.getLimo(id);
    }

    @RequestMapping(value = "/limo")
    public List<LimoDTO> getAllLimos() {
        return service.getAllLimos();
    }

    @RequestMapping(value = "/ticket")
    public List<LimoTicketDTO> getAllTickets() {
        return service.getAllTickets();
    }

    @RequestMapping(value = "/ticket/{id}")
    public LimoTicketDTO getTicket(@PathVariable Long id) {
        return service.getTicket(id);
    }

}
