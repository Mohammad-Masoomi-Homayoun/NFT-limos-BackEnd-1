package com.niftylimos.domain;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String tx;

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne
    private Limo limo;

    @OneToMany(mappedBy = "reservation", fetch = FetchType.LAZY)
    private Set<LimoTicket> tickets = new HashSet<>();

    public Reservation(Account account){
        this.account = account;
    }

}
