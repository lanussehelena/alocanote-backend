package com.alocanote.api.controller;

import com.alocanote.api.dto.request.RegisterUserRequestDTO;
import com.alocanote.api.dto.response.UserResponseDTO;
import com.alocanote.api.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterUserRequestDTO requestDTO) {
        UserResponseDTO newUser = userService.register(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> listarTodos() {
        List<UserResponseDTO> users = userService.findAllDTOs();
        return ResponseEntity.ok(users);
    }
}