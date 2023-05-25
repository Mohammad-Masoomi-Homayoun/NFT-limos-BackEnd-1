package com.niftylimos.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * handle Single Page Application routing
 */
@Controller
public class RoutingController {

    @RequestMapping("/faq")
    public ResponseEntity<String> faq() {
        return getIndex();
    }

    @RequestMapping("/terms")
    public ResponseEntity<String> terms() {
        return getIndex();
    }

    @RequestMapping("/3d")
    public ResponseEntity<String> d3() {
        return getIndex();
    }


    private ResponseEntity<String> getIndex() {
        try {
            // Jar
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("/static/index.html");
            // IDE
            if (inputStream == null) {
                inputStream = this.getClass().getResourceAsStream("/static/index.html");
            }
            String body = StreamUtils.copyToString(inputStream, Charset.defaultCharset());
            return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(body);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error in redirecting to index");
        }
    }


}
