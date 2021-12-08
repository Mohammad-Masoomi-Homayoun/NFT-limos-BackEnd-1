package com.niftylimos.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.niftylimos.service.NiftyLimosService;
import com.niftylimos.service.dto.AccountDTO;
import com.niftylimos.service.dto.LimoMetadataDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/")
public class NiftyLimosPublicController {

    private static final Logger logger = LoggerFactory.getLogger(NiftyLimosPublicController.class);

    private final NiftyLimosService service;

    public NiftyLimosPublicController(NiftyLimosService service) {
        this.service = service;
    }


    @RequestMapping(value = "/contract", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getContractAddress() throws JsonProcessingException {
        Map<String, Object> out = new HashMap<>();
        out.put("address", service.getContractAddress());
        ObjectMapper mapper = new ObjectMapper();
        var abi =mapper.readValue(service.getContractAbi(), List.class);
        out.put("abi", abi);
        return out;
    }

    @RequestMapping(value = "/account/{id}")
    public AccountDTO getAccount(@PathVariable String id) {
        return service.getAccount(id);
    }

    @RequestMapping(value = "/reservation/count")
    public Long getReservation() {
        return service.getReservationCount();
    }

    @RequestMapping(value = "/minted")
    public Long getMintedCount() {
        return service.getMintedCount();
    }

    @RequestMapping(value = "/limo/{id}")
    public LimoMetadataDTO getLimo(@PathVariable Long id) {
        return service.getLimoMetadata(id);
    }

    @RequestMapping(value = "/limos")
    public List<String> getLimoSignature() {
        return service.getLimos();
    }
}
