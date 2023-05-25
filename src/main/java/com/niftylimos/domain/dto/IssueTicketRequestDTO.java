package com.niftylimos.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IssueTicketRequestDTO {
    String address;
    Long limo;
    Long expire;
}
