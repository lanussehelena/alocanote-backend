package com.alocanote.api.security;

import com.alocanote.api.model.entity.User;
import com.alocanote.api.model.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "secret", "test-secret-key-1234567890");
        ReflectionTestUtils.setField(jwtTokenProvider, "expiration", 3600000L); // 1 hora
    }

    @Test
    @DisplayName("Deve gerar token JWT com sucesso para um User")
    void shouldGenerateTokenForUser() {
        User user = User.builder()
                .id(1L)
                .name("Maria Silva")
                .email("maria@alocanote.com")
                .role(Role.ADMINISTRADOR)
                .accessLevel("ROLE_ADMIN")
                .build();

        String token = jwtTokenProvider.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    @DisplayName("Deve validar token JWT e extrair subject com sucesso")
    void shouldValidateTokenAndExtractSubject() {
        String email = "maria@alocanote.com";
        String token = jwtTokenProvider.generateToken(email);

        String subject = jwtTokenProvider.validateToken(token);

        assertEquals(email, subject);
        assertTrue(jwtTokenProvider.isTokenValid(token));
        assertEquals(email, jwtTokenProvider.getSubject(token));
    }

    @Test
    @DisplayName("Deve retornar null ao validar token inválido ou corrompido")
    void shouldReturnNullForInvalidToken() {
        String invalidToken = "invalid.token.here";

        String subject = jwtTokenProvider.validateToken(invalidToken);

        assertNull(subject);
        assertFalse(jwtTokenProvider.isTokenValid(invalidToken));
    }

    @Test
    @DisplayName("Deve retornar null para token nulo ou vazio")
    void shouldReturnNullForNullOrEmptyToken() {
        assertNull(jwtTokenProvider.validateToken(null));
        assertNull(jwtTokenProvider.validateToken("   "));
        assertFalse(jwtTokenProvider.isTokenValid(null));
        assertFalse(jwtTokenProvider.isTokenValid(""));
    }
}
