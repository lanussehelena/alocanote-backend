package com.alocanote.api.controller;

import com.alocanote.api.dto.response.LocationResponseDTO;
import com.alocanote.api.service.LocationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationControllerTest {

    @Mock
    private LocationService locationService;

    @InjectMocks
    private LocationController locationController;

    @Test
    @DisplayName("Deve listar todos os locais retornando lista de LocationResponseDTO")
    void shouldListAllLocations() {
        LocationResponseDTO loc1 = new LocationResponseDTO(1L, "Loja Centro", "Rua A", "58000-000");
        LocationResponseDTO loc2 = new LocationResponseDTO(2L, "Showroom Sul", "Av B", "58000-111");

        when(locationService.findAll()).thenReturn(List.of(loc1, loc2));

        ResponseEntity<List<LocationResponseDTO>> response = locationController.getAllLocations();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Loja Centro", response.getBody().get(0).name());
        verify(locationService).findAll();
    }

    @Test
    @DisplayName("Deve buscar local por CEP com sucesso retornando HTTP 200")
    void shouldFindLocationByCepSuccessfully() {
        String cep = "58000-000";
        LocationResponseDTO loc = new LocationResponseDTO(1L, "Loja Centro", "Rua A", cep);

        when(locationService.findByCep(cep)).thenReturn(Optional.of(loc));

        ResponseEntity<LocationResponseDTO> response = locationController.getLocationByCep(cep);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Loja Centro", response.getBody().name());
    }

    @Test
    @DisplayName("Deve retornar HTTP 404 quando o CEP não for encontrado")
    void shouldReturn404WhenCepNotFound() {
        String cep = "00000-000";
        when(locationService.findByCep(cep)).thenReturn(Optional.empty());

        ResponseEntity<LocationResponseDTO> response = locationController.getLocationByCep(cep);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
}
