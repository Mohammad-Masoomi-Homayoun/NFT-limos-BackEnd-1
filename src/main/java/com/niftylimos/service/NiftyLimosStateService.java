package com.niftylimos.service;

import com.niftylimos.domain.NiftyLimosState;
import com.niftylimos.repo.NiftyLimosStateRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class NiftyLimosStateService {

    private final NiftyLimosStateRepository repository;

    public NiftyLimosStateService(NiftyLimosStateRepository repository) {
        this.repository = repository;
    }

    NiftyLimosState get(String key) {
        return repository.findById(key).orElse(repository.save(new NiftyLimosState(key, null)));
    }

    NiftyLimosState set(String key, String value) {
        var entry = repository.findById(key).orElse(repository.save(new NiftyLimosState(key, value)));
        entry.setNiftyLimosValue(value);
        return repository.save(entry);
    }
}
