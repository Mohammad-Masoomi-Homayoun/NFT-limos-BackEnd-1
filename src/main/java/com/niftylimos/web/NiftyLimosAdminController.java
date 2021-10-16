package com.niftylimos.web;

import com.niftylimos.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class NiftyLimosAdminController {

    private static final Logger logger = LoggerFactory.getLogger(NiftyLimosAdminController.class);

    private final NiftyLimosService service;

    public NiftyLimosAdminController(NiftyLimosService service) {
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

    @RequestMapping(value = "/limo")
    public List<LimoDTO> getAllLimos() {
        return service.getAllLimos();
    }

    @RequestMapping(value = "/limo/{id}")
    public LimoDTO getLimo(@PathVariable Long id) {
        return service.getLimo(id);
    }

    @RequestMapping(value = "/ticket")
    public List<LimoTicketDTO> getAllTickets() {
        return service.getAllTickets();
    }

    @RequestMapping(value = "/ticket/{id}")
    public LimoTicketDTO getTicket(@PathVariable Long id) {
        return service.getTicket(id);
    }

    @RequestMapping(value = "/issueTicket")
    public LimoTicketDTO issueTicket(@RequestParam String secret, @RequestParam String address, @RequestParam Long tokenId, @RequestParam Long expire) {
        checkSecret(secret);
        return service.issueTicket(address, tokenId, expire);
    }

    @RequestMapping(value = "/issueTicketAll")
    public List<LimoTicketDTO> issueTicketForAllReservations(@RequestParam String secret) {
        checkSecret(secret);
        return service.issueTicketForAllReservations();
    }

    @RequestMapping(value = "/config")
    public Long setExpire(@RequestParam String secret, @RequestParam Long newExpire) {
        checkSecret(secret);
        return service.setExpire(newExpire);
    }

    private void checkSecret(String secret){
        if(!secret.equals("Nas7079")){
            throw new RuntimeException("Forbidden!");
        }
    }
}
