package com.alocanote.api.service;

import com.alocanote.api.dto.request.RegisterUserRequestDTO;
import com.alocanote.api.dto.request.UpdateUserProfileRequestDTO;
import com.alocanote.api.dto.response.UserResponseDTO;
import com.alocanote.api.exception.BusinessException;
import com.alocanote.api.exception.ResourceNotFoundException;
import com.alocanote.api.model.entity.User;
import com.alocanote.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthService ignoredAuthService) {
        this(userRepository, passwordEncoder);
    }

    @Transactional
    public UserResponseDTO register(RegisterUserRequestDTO dto) {
        User saved = createUser(dto);
        return UserResponseDTO.fromEntity(saved);
    }

    @Transactional
    public User createUser(RegisterUserRequestDTO dto) {
        String normalizedEmail = dto.email().trim().toLowerCase();
        String normalizedPhone = (dto.phone() != null && !dto.phone().isBlank()) ? dto.phone().trim() : null;

        if (userRepository.existsByEmail(normalizedEmail) || userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            User existing = userRepository.findByEmail(normalizedEmail)
                    .or(() -> userRepository.findByEmailIgnoreCase(normalizedEmail))
                    .orElse(null);
            if (existing != null && (existing.getPassword() == null || existing.getPassword().isBlank())) {
                existing.setPassword(passwordEncoder.encode(dto.password()));
                existing.setAuthProvider("EMAIL");
                existing.setEmailVerified(true);
                if (dto.name() != null && !dto.name().isBlank()) {
                    existing.setName(dto.name().trim());
                }
                if (normalizedPhone != null && (existing.getPhone() == null || existing.getPhone().isBlank())) {
                    existing.setPhone(normalizedPhone);
                }
                if (dto.role() != null) {
                    existing.setRole(dto.role());
                }
                return userRepository.save(existing);
            }
            throw new BusinessException("Já existe um usuário cadastrado com este e-mail.");
        }

        if (normalizedPhone != null && userRepository.existsByPhone(normalizedPhone)) {
            throw new BusinessException("Já existe um usuário cadastrado com este telefone.");
        }

        com.alocanote.api.model.enums.Role resolvedRole = dto.role() != null
                ? dto.role()
                : com.alocanote.api.model.enums.Role.PROJETISTA;

        User user = User.builder()
                .name(dto.name().trim())
                .email(normalizedEmail)
                .phone(normalizedPhone)
                .role(resolvedRole)
                .customRole(dto.customRole())
                .password(passwordEncoder.encode(dto.password()))
                .authProvider("EMAIL")
                .emailVerified(true)
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
        User user = resolveCurrentUser(email);
        return UserResponseDTO.fromEntity(user);
    }

    @Transactional
    public UserResponseDTO updateCurrentUser(String email, UpdateUserProfileRequestDTO dto) {
        User existingUser = resolveCurrentUser(email);

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
            existingUser.setProfilePictureUrl(dto.profilePictureUrl().trim());
        }

        if (dto.role() != null) {
            existingUser.setRole(dto.role());
            existingUser.setAccessLevel(dto.role() == com.alocanote.api.model.enums.Role.ADMINISTRADOR ? "ROLE_ADMIN" : "ROLE_USER");
        }

        if (dto.customRole() != null) {
            existingUser.setCustomRole(dto.customRole().trim());
        } else if (dto.role() != null && dto.role() != com.alocanote.api.model.enums.Role.OUTROS) {
            existingUser.setCustomRole(null);
        }

        if (dto.street() != null) {
            existingUser.setStreet(dto.street().trim());
        }

        if (dto.number() != null) {
            existingUser.setNumber(dto.number().trim());
        }

        if (dto.neighborhood() != null) {
            existingUser.setNeighborhood(dto.neighborhood().trim());
        }

        if (dto.city() != null) {
            existingUser.setCity(dto.city().trim());
        }

        if (dto.state() != null) {
            existingUser.setState(dto.state().trim());
        }

        if (dto.zipCode() != null) {
            existingUser.setZipCode(dto.zipCode().trim());
        }

        if (dto.newPassword() != null && !dto.newPassword().isBlank()) {
            if (existingUser.getPassword() != null && !existingUser.getPassword().isBlank()) {
                if (dto.currentPassword() == null || dto.currentPassword().isBlank()) {
                    throw new BusinessException("Informe a senha atual para alterar a senha.");
                }
                if (!passwordEncoder.matches(dto.currentPassword(), existingUser.getPassword())) {
                    throw new BusinessException("A senha atual está incorreta.");
                }
            }
            existingUser.setPassword(passwordEncoder.encode(dto.newPassword()));
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

    private User resolveCurrentUser(String email) {
        if (email != null && !email.isBlank()) {
            String normalizedEmail = email.trim().toLowerCase();
            return userRepository.findByEmail(normalizedEmail)
                    .or(() -> userRepository.findByEmailIgnoreCase(normalizedEmail))
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado não encontrado."));
        }
        List<User> allUsers = userRepository.findAll();
        return allUsers.stream()
                .filter(u -> u.getEmail() != null && !u.getEmail().equalsIgnoreCase("admin@alocanote.com"))
                .reduce((first, second) -> second)
                .or(() -> allUsers.stream().findFirst())
                .orElseThrow(() -> new BusinessException("Usuário não autenticado."));
    }
}