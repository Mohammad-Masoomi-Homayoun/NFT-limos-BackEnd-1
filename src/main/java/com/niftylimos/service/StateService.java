package com.niftylimos.service;

import com.niftylimos.domain.NiftyLimosState;
import com.niftylimos.repo.NiftyLimosStateRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@Transactional
public class StateService {

    private final NiftyLimosStateRepository repository;

    public StateService(NiftyLimosStateRepository repository) {
        this.repository = repository;
    }

    Optional<String> get(String key) {
        return repository.findById(key).map(NiftyLimosState::getNiftyLimosValue);
    }

    void set(String key, String value) {
        var entry = repository.findById(key);
        if (entry.isPresent()) {
            entry.get().setNiftyLimosValue(value);
            repository.save(entry.get());
        } else {
            repository.save(new NiftyLimosState(key, value));
        }
        repository.flush();
    }
}
