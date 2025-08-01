package com.example.identifyService.service;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.function.Function;

@Component
public class IdGeneratorService {
    public String generateRandomId(String prefix, Function<String, Boolean> existsByIdFunc) {
        String id;
        do {
            id = prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 5).toUpperCase();
        } while (existsByIdFunc.apply(id));
        return id;
    }
}