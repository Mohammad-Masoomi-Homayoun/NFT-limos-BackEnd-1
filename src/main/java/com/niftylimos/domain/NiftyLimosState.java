package com.niftylimos.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@NoArgsConstructor
@Entity
public class NiftyLimosState {

    @Id
    private String niftyLimosKey;

    private String niftyLimosValue;

    public NiftyLimosState(String key, String value){
        this.niftyLimosKey = key;
        this.niftyLimosValue = value;
    }
}
