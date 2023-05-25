package com.niftylimos.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LimoDTO {

    private Long id;

    private String status;

    private List<Long> reservations;

    private List<Long> tickets;

    private String image;
}
