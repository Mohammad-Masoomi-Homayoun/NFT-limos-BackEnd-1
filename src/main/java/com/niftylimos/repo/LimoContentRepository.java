package com.niftylimos.repo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;

@Component
public class LimoContentRepository {

//    private static final Logger logger = LoggerFactory.getLogger(LimoContentRepository.class);
//
//    @Value("${niftylimos.LimoContentRepository.path}")
//    private String root;
//
//    private String imgRoot;
//
//    @PostConstruct
//    private void init(){
//        if(this.root == null){
//            throw new RuntimeException("path is null");
//        }
//        this.imgRoot = this.root + "img";
//        new File(this.imgRoot).mkdir();
//    }

}
