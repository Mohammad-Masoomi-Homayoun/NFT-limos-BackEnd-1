package com.niftylimos.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReserveStatus {
    private long timestamp;
    private long numReserved;
    private List<String> accounts;
}
