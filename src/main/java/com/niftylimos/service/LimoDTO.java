package com.niftylimos.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LimoDTO {
//    private long id;
//    private String name;
//    private String description;
//    private String image;
//    private String external_url;
//    private List attributes;

    private Long id;

    private String status;

    private List<Long> reservations;

    private List<Long> tickets;

    private String image;
}
