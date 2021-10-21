package com.niftylimos.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class LimoData {

    @Id
    private Long id;

    @OneToOne
    private Limo limo;

    @Column(name = "_image")
    private String image;

    @OneToMany(fetch = FetchType.EAGER)
    private Set<LimoAttr> attributes = new HashSet<>();

}
