package com.niftylimos.limos;

import com.niftylimos.domain.LimoData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Statistics {

    public static LimoDto toLimo(LimoData limoData){
        LimoDto limoDto = new LimoDto();

        if(limoData.getBody() != null){
            limoDto.getAttributes().put("Body", limoData.getBody());
        }

        if(limoData.getRing() != null){
            limoDto.getAttributes().put("Ring", limoData.getRing());
        }

        if(limoData.getTrunk() != null){
            limoDto.getAttributes().put("Trunk", limoData.getTrunk());
        }

        if(limoData.getRoof() != null){
            limoDto.getAttributes().put("Roof", limoData.getRoof());
        }

        if(limoData.getFootstep() != null){
            limoDto.getAttributes().put("Footstep", limoData.getFootstep());
        }

        if(limoData.getDoor() != null){
            limoDto.getAttributes().put("Door", limoData.getDoor());
        }

        if(limoData.getMirror() != null){
            limoDto.getAttributes().put("Mirror", limoData.getMirror());
        }

        if(limoData.getHood() != null){
            limoDto.getAttributes().put("Hood", limoData.getHood());
        }

        if(limoData.getBumper() != null){
            limoDto.getAttributes().put("Bumper", limoData.getBumper());
        }

        return limoDto;
    }

    public static Map<String, Map<String, Double>> calcTraitStatistics(Collection<LimoData> limoData){
        Map<String, Map<String, Double>> traitRarity = new HashMap<>();
        var limos = limoData.stream().map(Statistics::toLimo).collect(Collectors.toSet());
        for (var limo : limos){
            for (var attr : limo.getAttributes().entrySet()){
                traitRarity.putIfAbsent(attr.getKey(), new HashMap<>());
                traitRarity.get(attr.getKey()).putIfAbsent(attr.getValue(), 0.0);
                traitRarity.get(attr.getKey()).compute(attr.getValue(),(k, v) -> v + 1);
            }
        }

        traitRarity.values().forEach(t -> t.forEach((k, v) -> t.compute(k, (kk, vv) -> vv / limoData.size())));

        return traitRarity;
    }

    public static Map<String, Rank> calcLimoRank(Collection<LimoData> limos){
        var traits = calcTraitStatistics(limos);
        //todo
        return null;
    }
}
