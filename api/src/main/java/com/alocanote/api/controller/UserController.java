package com.alocanote.api.controller;

import com.alocanote.api.dto.request.RegisterUserRequestDTO;
import com.alocanote.api.model.entity.User;
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

    // Endpoint para registrar as colaboradoras da loja
    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterUserRequestDTO requestDTO) {
        // Chama a regra de negócio completa que a Tamires criou
        User newUser = userService.createUser(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    // Endpoint para listar as usuárias
    @GetMapping
    public ResponseEntity<List<User>> listarTodos() {
        List<User> users = userService.findAll();
        return ResponseEntity.ok(users);
    }
}