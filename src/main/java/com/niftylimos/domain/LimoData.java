package com.niftylimos.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class LimoData {

    @Id
    private Long id;

    @OneToOne
    private Limo limo;

    private String image;

    @OneToMany
    private List<LimoAttr> attributes = new ArrayList<>();

}
