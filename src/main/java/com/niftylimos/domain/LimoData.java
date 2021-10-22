package com.niftylimos.domain;

import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "limo_data")
public class LimoData {

    @Id
    private Long id;

    @OneToOne
    private Limo limo;

    @Column(name = "image")
    private String image;

    @OneToMany(fetch = FetchType.LAZY)
    private Set<LimoAttr> attributes = new HashSet<>();

}
