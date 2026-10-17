package com.alocanote.api.service;

import com.alocanote.api.dto.response.LocationResponseDTO;
import com.alocanote.api.exception.ResourceNotFoundException;
import com.alocanote.api.model.entity.Location;
import com.alocanote.api.repository.LocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class LocationService {

    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Transactional(readOnly = true)
    public List<LocationResponseDTO> findAll() {
        return locationRepository.findAll().stream()
                .map(LocationResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<LocationResponseDTO> findByCep(String cep) {
        return locationRepository.findByCep(cep)
                .map(LocationResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public Location findById(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Local de retirada não encontrado com o ID: " + id));
    }
}
