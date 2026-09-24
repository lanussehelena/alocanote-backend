package com.alocanote.api.service;

import com.alocanote.api.dto.request.RegisterUserRequestDTO;
import com.alocanote.api.model.entity.User;
import com.alocanote.api.repository.UserRepository;
import com.alocanote.api.exception.BusinessException; // Exemplo de exceção personalizada
import com.alocanote.api.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    // Injeção de dependência via construtor
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User createUser(RegisterUserRequestDTO dto) {
        // Validação de Unicidade de Email
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("Já existe um usuário cadastrado com este e-mail.");
        }

        // Validação de Unicidade de Telefone
        if (userRepository.existsByPhone(dto.getPhone())) {
            throw new BusinessException("Já existe um usuário cadastrado com este telefone.");
        }

        // Transforma o DTO na Entidade User usando o Builder
        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .role(dto.getRole())
                .customRole(dto.getCustomRole())
                .build();

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com o ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public User updateUser(Long id, User userDetails) {
        User existingUser = findById(id);

        existingUser.setName(userDetails.getName());
        existingUser.setPhone(userDetails.getPhone());
        existingUser.setStreet(userDetails.getStreet());

        return userRepository.save(existingUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = findById(id);
        userRepository.delete(user);
    }
}