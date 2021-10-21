package com.niftylimos.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
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

    @OneToMany(fetch = FetchType.EAGER)
    private Set<LimoAttr> attributes = new HashSet<>();

}
