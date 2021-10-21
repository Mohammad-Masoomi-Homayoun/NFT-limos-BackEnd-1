package com.niftylimos.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@Entity
public class Reservation {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    @Column(name = "_account")
    private Account account;

    @ManyToOne
    @Column(name = "_limo")
    private Limo limo;

    @OneToMany(mappedBy = "reservation", fetch = FetchType.EAGER)
    private Set<LimoTicket> tickets = new HashSet<>();

    public Reservation(Account account){
        this.account = account;
    }
}
