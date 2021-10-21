package com.niftylimos;

import com.niftylimos.service.NiftyLimosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NiftylimosApplication implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(NiftylimosApplication.class, args);
    }



    @Override
    public void run(String... args) throws Exception {

    }
}
