package com.niftylimos.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@Entity
public class Account {
    @Id
    private String address;

    @OneToMany(mappedBy = "account", fetch = FetchType.EAGER)
    private Set<Reservation> reservations = new HashSet<>();

    @OneToMany(mappedBy = "account", fetch = FetchType.EAGER)
    private Set<LimoTicket> tickets = new HashSet<>();

    public Account(String address){
        this.address = address;
    }
}
