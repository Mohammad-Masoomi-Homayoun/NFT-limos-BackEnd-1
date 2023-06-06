package com.niftylimos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.transaction.Transactional;

@SpringBootApplication
@EnableScheduling
@Transactional
public class NiftylimosApplication {

    public static void main(String[] args) {
        SpringApplication.run(NiftylimosApplication.class, args);
    }

}

