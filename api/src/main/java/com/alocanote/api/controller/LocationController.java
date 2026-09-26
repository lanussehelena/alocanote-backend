package com.alocanote.api.controller;

import com.alocanote.api.model.entity.Location;
import com.alocanote.api.repository.LocationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationRepository locationRepository;

    public LocationController(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @GetMapping
    public ResponseEntity> getAllLocations() {
        List locations = locationRepository.findAll();
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/cep/{cep}")
    public ResponseEntity getLocationByCep(@PathVariable String cep) {
        return locationRepository.findByCep(cep)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}