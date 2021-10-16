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
    private String key;

    private String value;

    public NiftyLimosState(String key, String value){
        this.key = key;
        this.value = value;
    }
}
