package com.niftylimos.service;

import java.util.Optional;

public interface StateService {
    Optional<String> get(String key);

    void set(String key, String value);
}
