package com.niftylimos.service.impl;

import com.niftylimos.domain.NiftyLimosState;
import com.niftylimos.repository.NiftyLimosStateRepository;
import com.niftylimos.service.StateService;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@Transactional
public class StateServiceImpl implements StateService {

    private final NiftyLimosStateRepository repository;

    public StateServiceImpl(NiftyLimosStateRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<String> get(String key) {
        return repository.findById(key).map(NiftyLimosState::getNiftyLimosValue);
    }

    @Override
    public void set(String key, String value) {
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
