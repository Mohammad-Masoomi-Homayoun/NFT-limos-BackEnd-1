package com.niftylimos.domain;


import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

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
    private List<LimoTicket> tickets = new ArrayList<>();


    public Limo(Long tokenId) {
        this.id = tokenId;
    }
}
