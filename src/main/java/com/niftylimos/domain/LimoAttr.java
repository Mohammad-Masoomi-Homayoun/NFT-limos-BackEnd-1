package com.niftylimos.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class LimoAttr {

    @Id
    private Long id;

    @Enumerated
    @Column(name = "_type")
    private LimoAttrType type;

    @Column(name = "_value")
    private String value;
}
