package com.niftylimos.limos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LimoDto {
    private String signature;
    private Map<String, String> attributes = new HashMap<>();
    private Rank rank = new Rank();
}
