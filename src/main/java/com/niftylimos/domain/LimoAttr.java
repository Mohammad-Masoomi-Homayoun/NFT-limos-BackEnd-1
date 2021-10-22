package com.niftylimos.domain;


import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "limo_attributes")
public class LimoAttr {

    @Id
    private Long id;

    @Enumerated
    @Column(name = "_type")
    private LimoAttrType type;

    @Column(name = "_value")
    private String value;

}
