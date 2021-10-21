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
public class Limo {

    @Id
    private Long id;

    private String status;

    @OneToOne(mappedBy = "limo", fetch = FetchType.EAGER)
    private LimoData data;

    @OneToMany(mappedBy = "limo", fetch = FetchType.EAGER)
    private List<Reservation> reservations = new ArrayList<>();

    @OneToMany(mappedBy = "limo", fetch = FetchType.EAGER)
    private Set<LimoTicket> tickets = new HashSet<>();


    public Limo(Long tokenId) {
        this.id = tokenId;
    }
}