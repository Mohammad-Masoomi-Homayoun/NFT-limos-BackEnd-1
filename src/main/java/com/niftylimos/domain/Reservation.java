package com.niftylimos.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
public class Reservation {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private Account account;

    @ManyToOne
    private Limo limo;

    @OneToMany(mappedBy = "reservation")
    private List<LimoTicket> tickets = new ArrayList<>();

    public Reservation(Account account){
        this.account = account;
    }
}
