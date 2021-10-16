package com.niftylimos.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LimoTicketDTO {
    private Long id;
    private Long reservation;
    private String account;
    private Long limo;
    private String v;
    private String r;
    private String s;
    private Long expire;
}
