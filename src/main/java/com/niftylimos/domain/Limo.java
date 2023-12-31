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
@Table(name = "limos")
public class Limo {

    @Id
    private Long id;

    @Column(name = "state")
    private String status;

    @OneToOne(mappedBy = "limo", fetch = FetchType.LAZY)
    private LimoData data;

    @OneToMany(mappedBy = "limo", fetch = FetchType.LAZY)
    private Set<Reservation> reservations = new HashSet<>();

    @OneToMany(mappedBy = "limo", fetch = FetchType.LAZY)
    private Set<LimoTicket> tickets = new HashSet<>();

    public Limo(Long tokenId) {
        this.id = tokenId;
    }

}
