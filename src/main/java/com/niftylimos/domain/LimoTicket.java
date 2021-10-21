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
    @Column(name = "_reservation")
    private Reservation reservation;

    @ManyToOne
    @Column(name = "_account")
    private Account account;

    @ManyToOne(optional = false)
    @Column(name = "_limo")
    private Limo limo;

    @Column(name ="_expire", nullable = false)
    private Long expire;

    @Column(name = "_v", nullable = false)
    private String v;

    @Column(name = "_r", nullable = false)
    private String r;

    @Column(name = "_s", nullable = false)
    private String s;


}
