package com.niftylimos.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@Entity
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne
    private Limo limo;

    @OneToMany(mappedBy = "reservation", fetch = FetchType.EAGER)
    private Set<LimoTicket> tickets = new HashSet<>();

    public Reservation(Account account){
        this.account = account;
    }
}
