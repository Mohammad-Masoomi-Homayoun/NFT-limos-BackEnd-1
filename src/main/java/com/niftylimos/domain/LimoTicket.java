package com.niftylimos.domain;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "limo_tickets")
public class LimoTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TICKET_SEQ")
    private Long id;

    @ManyToOne
    private Reservation reservation;

    @ManyToOne(optional = false)
    @JoinColumn(name = "_account")
    private Account account;

    @ManyToOne(optional = false)
    private Limo limo;

    @Column(name ="_expire", nullable = false)
    private Long expire;

    @Column(name = "_signature", nullable = false)
    private String signature;
}
