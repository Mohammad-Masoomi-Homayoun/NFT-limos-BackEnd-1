package com.niftylimos.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountDTO {
    private String address;
    private List<Long> reservations;
    private List<LimoTicketDTO> tickets;
}
