package com.niftylimos.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
public class Account {
    @Id
    private String address;

    @OneToMany(mappedBy = "account")
    private List<Reservation> reservations = new ArrayList<>();

    @OneToMany(mappedBy = "account")
    private List<LimoTicket> tickets = new ArrayList<>();

    public Account(String address){
        this.address = address;
    }
}
