package com.alocanote.api.controller;

import com.alocanote.api.dto.response.LocationResponseDTO;
import com.alocanote.api.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public ResponseEntity<List<LocationResponseDTO>> getAllLocations() {
        List<LocationResponseDTO> locations = locationService.findAll();
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/cep/{cep}")
    public ResponseEntity<LocationResponseDTO> getLocationByCep(@PathVariable String cep) {
        return locationService.findByCep(cep)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}