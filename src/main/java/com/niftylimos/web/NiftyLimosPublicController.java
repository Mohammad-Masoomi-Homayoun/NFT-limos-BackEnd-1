package com.niftylimos.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.niftylimos.service.dto.AccountDTO;
import com.niftylimos.service.dto.LimoMetadataDTO;
import com.niftylimos.service.NiftyLimosService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
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


    @RequestMapping(value = "/limo/{id}")
    public LimoMetadataDTO getLimo(@PathVariable Long id) {
        return service.getLimoMetadata(id);
    }

    @RequestMapping(value = "/limo/image/{id}")
    public void getImg(@PathVariable("id") String id, HttpServletResponse response) {
        try {
            response.setHeader("Content-disposition", "attachment; filename=" + id);
            response.setContentType("image/jpeg");
            InputStream in = new ByteArrayInputStream(service.getLimoImage(id));
            IOUtils.copy(in, response.getOutputStream());
            response.flushBuffer();
        } catch (IOException ex) {
            throw new RuntimeException("IOError writing file to output stream");
        }
    }

    @RequestMapping(value = "/limo/animation/{id}")
    public void getAnimation(@PathVariable("id") String id, HttpServletResponse response) {
        try {
            response.setHeader("Content-disposition", "attachment; filename=" + id);
            response.setContentType("video/mp4");
            InputStream in = new ByteArrayInputStream(service.getLimoAnimation(id));
            IOUtils.copy(in, response.getOutputStream());
            response.flushBuffer();
        } catch (IOException ex) {
            throw new RuntimeException("IOError writing file to output stream");
        }
    }
}
