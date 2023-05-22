package com.niftylimos.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "limo_data")
public class LimoData {
    @Id
    private Long id;

    @Column(nullable = false, unique = true)
    private String signature;


    @Column(nullable = false)
    private String body;

    @Column(nullable = false)
    private String ring;

    private String trunk;

    @Column(nullable = false)
    private String roof;

    private String footstep;

    private String door;

    @Column(nullable = false)
    private String mirror;

    private String hood;

    private String bumper;

    @OneToOne
    @JoinColumn(unique = true)
    private Limo limo;
}
