package com.niftylimos.limos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Rank {
    private double trait;
    private double average;
    private double statistical;
    private double score;
}
