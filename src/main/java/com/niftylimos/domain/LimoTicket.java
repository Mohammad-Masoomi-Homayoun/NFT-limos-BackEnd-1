package com.niftylimos.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
public class LimoTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    private Reservation reservation;

    @ManyToOne
    private Account account;

    @ManyToOne(optional = false)
    private Limo limo;

    @Column(nullable = false)
    private Long expire;

    @Column(nullable = false)
    private String v;

    @Column(nullable = false)
    private String r;

    @Column(nullable = false)
    private String s;


}
