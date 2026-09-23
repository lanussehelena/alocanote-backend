package com.alocanote.api.service;

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

    // 1. Boa Prática: Injeção de dependência via construtor (sem @Autowired)
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 2. Boa Prática: Uso de @Transactional para operações de escrita
    @Transactional
    public User createUser(User user) {
        // Validação de Unicidade de Email
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new BusinessException("Já existe um usuário cadastrado com este e-mail.");
        }

        // Validação de Unicidade de Telefone
        if (userRepository.existsByPhone(user.getPhone())) {
            throw new BusinessException("Já existe um usuário cadastrado com este telefone.");
        }

        return userRepository.save(user);
    }

    // 3. Boa Prática: Métodos de leitura otimizados com @Transactional(readOnly = true)
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

        // Atualizar campos necessários com validações pertinentes
        existingUser.setName(userDetails.getName());
        existingUser.setPhone(userDetails.getPhone());
        existingUser.setStreet(userDetails.getStreet());
        // ... atualizar outros campos conforme necessário

        return userRepository.save(existingUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = findById(id);
        userRepository.delete(user);
    }
}