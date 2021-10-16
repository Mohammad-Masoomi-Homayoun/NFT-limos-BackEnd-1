package com.niftylimos.domain;


import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
public class Limo {

    @Id
    private Long id;

    private String status;

    @OneToOne(mappedBy = "limo")
    private LimoData data;

    @OneToMany(mappedBy = "limo")
    private List<Reservation> reservations = new ArrayList<>();

    @OneToMany(mappedBy = "limo")
    private List<LimoTicket> tickets = new ArrayList<>();


    public Limo(Long tokenId) {
        this.id = tokenId;
    }
}
