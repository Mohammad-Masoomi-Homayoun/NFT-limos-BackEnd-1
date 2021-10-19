package com.niftylimos.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LimoMetadataDTO {
    private String name;
    private String description;
    private String image;
    private String animation_url;
    private List<Object> attributes = new ArrayList<>();
}
