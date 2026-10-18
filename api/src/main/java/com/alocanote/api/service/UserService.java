package com.alocanote.api.service;

import com.alocanote.api.dto.request.RegisterUserRequestDTO;
import com.alocanote.api.dto.request.UpdateUserProfileRequestDTO;
import com.alocanote.api.dto.response.UserResponseDTO;
import com.alocanote.api.exception.BusinessException;
import com.alocanote.api.exception.ResourceNotFoundException;
import com.alocanote.api.model.entity.User;
import com.alocanote.api.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponseDTO register(RegisterUserRequestDTO dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new BusinessException("Já existe um usuário cadastrado com este e-mail.");
        }

        if (userRepository.existsByPhone(dto.phone())) {
            throw new BusinessException("Já existe um usuário cadastrado com este telefone.");
        }

        User user = User.builder()
                .name(dto.name())
                .email(dto.email())
                .phone(dto.phone())
                .role(dto.role())
                .customRole(dto.customRole())
                .password(passwordEncoder.encode(dto.password()))
                .build();

        User saved = userRepository.save(user);
        return UserResponseDTO.fromEntity(saved);
    }

    @Transactional
    public User createUser(RegisterUserRequestDTO dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new BusinessException("Já existe um usuário cadastrado com este e-mail.");
        }

        if (userRepository.existsByPhone(dto.phone())) {
            throw new BusinessException("Já existe um usuário cadastrado com este telefone.");
        }

        User user = User.builder()
                .name(dto.name())
                .email(dto.email())
                .phone(dto.phone())
                .role(dto.role())
                .customRole(dto.customRole())
                .password(passwordEncoder.encode(dto.password()))
                .build();

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com o ID: " + id));
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findDtoById(Long id) {
        return UserResponseDTO.fromEntity(findById(id));
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado não encontrado."));
        return UserResponseDTO.fromEntity(user);
    }

    @Transactional
    public UserResponseDTO updateCurrentUser(String email, UpdateUserProfileRequestDTO dto) {
        User existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado não encontrado."));

        if (dto.name() != null && !dto.name().isBlank()) {
            existingUser.setName(dto.name().trim());
        }

        if (dto.phone() != null && !dto.phone().isBlank()) {
            String newPhone = dto.phone().trim();
            if (!newPhone.equals(existingUser.getPhone()) && userRepository.existsByPhone(newPhone)) {
                throw new BusinessException("Já existe um usuário cadastrado com este telefone.");
            }
            existingUser.setPhone(newPhone);
        }

        if (dto.profilePictureUrl() != null) {
            existingUser.setProfilePictureUrl(dto.profilePictureUrl());
        }

        if (dto.customRole() != null) {
            existingUser.setCustomRole(dto.customRole());
        }

        if (dto.street() != null) {
            existingUser.setStreet(dto.street());
        }

        if (dto.number() != null) {
            existingUser.setNumber(dto.number());
        }

        if (dto.neighborhood() != null) {
            existingUser.setNeighborhood(dto.neighborhood());
        }

        if (dto.city() != null) {
            existingUser.setCity(dto.city());
        }

        if (dto.state() != null) {
            existingUser.setState(dto.state());
        }

        if (dto.zipCode() != null) {
            existingUser.setZipCode(dto.zipCode());
        }

        User saved = userRepository.save(existingUser);
        return UserResponseDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> findAllDTOs() {
        return userRepository.findAll().stream()
                .map(UserResponseDTO::fromEntity)
                .toList();
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