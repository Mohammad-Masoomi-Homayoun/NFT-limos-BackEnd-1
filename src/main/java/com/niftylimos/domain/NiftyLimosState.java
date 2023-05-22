package com.niftylimos.domain;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "niftylimos_state")
public class NiftyLimosState {

    @Id
    private String niftyLimosKey;

    private String niftyLimosValue;

    public NiftyLimosState(String key, String value){
        this.niftyLimosKey = key;
        this.niftyLimosValue = value;
    }
}
