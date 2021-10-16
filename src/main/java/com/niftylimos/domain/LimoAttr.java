package com.niftylimos.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private LimoAttrType type;

    private String value;
}
