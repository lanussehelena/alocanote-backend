package com.alocanote.api.security;

import com.alocanote.api.model.entity.User;
import com.alocanote.api.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalizedEmail = username != null ? username.trim().toLowerCase() : "";
        User user = userRepository.findByEmail(normalizedEmail)
                .or(() -> userRepository.findByEmailIgnoreCase(normalizedEmail))
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o e-mail: " + username));

        return buildUserDetails(user);
    }

    public UserDetails loadOrRecoverUserFromToken(String email, String name, String roleClaim, String accessLevelClaim) {
        String normalizedEmail = email != null ? email.trim().toLowerCase() : "";
        User user = userRepository.findByEmail(normalizedEmail)
                .or(() -> userRepository.findByEmailIgnoreCase(normalizedEmail))
                .orElseGet(() -> {
                    com.alocanote.api.model.enums.Role resolvedRole = com.alocanote.api.model.enums.Role.PROJETISTA;
                    if (roleClaim != null) {
                        try {
                            resolvedRole = com.alocanote.api.model.enums.Role.valueOf(roleClaim);
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                    User recovered = User.builder()
                            .name(name != null && !name.isBlank() ? name : "Colaborador")
                            .email(normalizedEmail)
                            .phone(null)
                            .password("")
                            .role(resolvedRole)
                            .accessLevel(accessLevelClaim != null && !accessLevelClaim.isBlank() ? accessLevelClaim : "ROLE_USER")
                            .build();
                    return userRepository.save(recovered);
                });

        return buildUserDetails(user);
    }

    private UserDetails buildUserDetails(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (user.getAccessLevel() != null && !user.getAccessLevel().isBlank()) {
            authorities.add(new SimpleGrantedAuthority(user.getAccessLevel()));
        } else if (user.getRole() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword() != null ? user.getPassword() : "",
                authorities
        );
    }
}
