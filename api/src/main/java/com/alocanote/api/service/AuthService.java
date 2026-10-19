package com.alocanote.api.service;

import com.alocanote.api.dto.request.FirebaseAuthRequestDTO;
import com.alocanote.api.dto.request.LoginRequestDTO;
import com.alocanote.api.dto.response.LoginResponseDTO;
import com.alocanote.api.exception.BusinessException;
import com.alocanote.api.model.entity.User;
import com.alocanote.api.model.enums.Role;
import com.alocanote.api.repository.UserRepository;
import com.alocanote.api.security.JwtTokenProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository) {
        this(userRepository, null, null);
    }

    @Autowired
    public AuthService(UserRepository userRepository,
                       JwtTokenProvider jwtTokenProvider,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO loginWithEmailAndPassword(LoginRequestDTO request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .or(() -> userRepository.findByEmailIgnoreCase(normalizedEmail))
                .orElseThrow(() -> new BusinessException("E-mail não encontrado. Verifique o endereço digitado."));

        if ((user.getPassword() == null || user.getPassword().isBlank())
                && "GOOGLE".equalsIgnoreCase(user.getAuthProvider())) {
            throw new BusinessException("Esta conta foi cadastrada sem senha. Acesse a tela de Cadastro com este mesmo e-mail para cadastrar sua senha.");
        }

        if (passwordEncoder != null && user.getPassword() != null && !user.getPassword().isBlank()
                && !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException("Senha incorreta. Tente novamente.");
        }

        return buildLoginResponse(user);
    }

    @Transactional
    public LoginResponseDTO authenticateWithFirebase(FirebaseAuthRequestDTO request) {
        if (request == null || request.idToken() == null || request.idToken().isBlank()) {
            throw new BusinessException("O token de autenticação do Firebase é obrigatório.");
        }

        FirebaseTokenClaims claims = verifyOrDecodeFirebaseToken(request);

        String rawEmail = (claims.email() != null && !claims.email().isBlank())
                ? claims.email()
                : request.email();

        if (rawEmail == null || rawEmail.isBlank()) {
            throw new BusinessException("Não foi possível identificar o e-mail no token do Firebase.");
        }

        String normalizedEmail = rawEmail.trim().toLowerCase();
        String resolvedName = resolveDisplayName(claims.name(), request.name(), normalizedEmail);
        String resolvedPicture = (claims.picture() != null && !claims.picture().isBlank())
                ? claims.picture()
                : request.profilePictureUrl();
        String resolvedPhone = (request.phone() != null && !request.phone().isBlank())
                ? request.phone().trim()
                : null;

        User user = findExistingUser(claims.uid(), normalizedEmail)
                .map(existing -> updateExistingUserFromFirebase(existing, claims, resolvedName, resolvedPicture, resolvedPhone, request))
                .orElseGet(() -> createNewUserFromFirebase(claims, normalizedEmail, resolvedName, resolvedPicture, resolvedPhone, request));

        return buildLoginResponse(user);
    }

    @Transactional
    public Optional<User> resolveUserFromFirebaseToken(String idToken) {
        try {
            FirebaseAuthRequestDTO dto = new FirebaseAuthRequestDTO(idToken);
            FirebaseTokenClaims claims = decodeTokenLenientForFilter(idToken);
            if (claims.email() == null || claims.email().isBlank()) {
                return Optional.empty();
            }
            String normalizedEmail = claims.email().trim().toLowerCase();
            String resolvedName = resolveDisplayName(claims.name(), null, normalizedEmail);
            User user = findExistingUser(claims.uid(), normalizedEmail)
                    .map(existing -> updateExistingUserFromFirebase(existing, claims, resolvedName, claims.picture(), null, dto))
                    .orElseGet(() -> createNewUserFromFirebase(claims, normalizedEmail, resolvedName, claims.picture(), null, dto));
            return Optional.of(user);
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private FirebaseTokenClaims decodeTokenLenientForFilter(String idToken) {
        if (!FirebaseApp.getApps().isEmpty()) {
            try {
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken.trim());
                String provider = extractProviderFromFirebaseMap(decodedToken.getClaims());
                return new FirebaseTokenClaims(
                        decodedToken.getUid(),
                        decodedToken.getEmail(),
                        decodedToken.getName(),
                        decodedToken.getPicture(),
                        decodedToken.isEmailVerified(),
                        provider
                );
            } catch (Exception ignored) {
                // Fallback to JWT decode below
            }
        }
        DecodedJWT jwt = JWT.decode(idToken.trim());
        String uid = jwt.getSubject();
        if (uid == null || uid.isBlank()) {
            uid = getStringClaim(jwt, "user_id");
        }
        String email = getStringClaim(jwt, "email");
        if ((email == null || email.isBlank()) && uid != null && uid.contains("@")) {
            email = uid;
        }
        String name = getStringClaim(jwt, "name");
        String picture = getStringClaim(jwt, "picture");
        Claim emailVerifiedClaim = jwt.getClaim("email_verified");
        boolean emailVerified = (emailVerifiedClaim != null && !emailVerifiedClaim.isNull())
                ? Boolean.TRUE.equals(emailVerifiedClaim.asBoolean())
                : true;
        String provider = "FIREBASE_EMAIL";
        Claim firebaseClaim = jwt.getClaim("firebase");
        if (firebaseClaim != null && !firebaseClaim.isNull()) {
            Map<String, Object> firebaseMap = firebaseClaim.asMap();
            provider = extractProviderFromMap(firebaseMap);
        } else if (jwt.getIssuer() != null && jwt.getIssuer().contains("accounts.google.com")) {
            provider = "GOOGLE";
        }
        return new FirebaseTokenClaims(uid, email, name, picture, emailVerified, provider);
    }

    private Optional<User> findExistingUser(String firebaseUid, String normalizedEmail) {
        if (firebaseUid != null && !firebaseUid.isBlank()) {
            Optional<User> byUid = userRepository.findByFirebaseUid(firebaseUid);
            if (byUid.isPresent()) {
                return byUid;
            }
        }
        return userRepository.findByEmail(normalizedEmail)
                .or(() -> userRepository.findByEmailIgnoreCase(normalizedEmail));
    }

    private User updateExistingUserFromFirebase(User user,
                                                FirebaseTokenClaims claims,
                                                String resolvedName,
                                                String resolvedPicture,
                                                String resolvedPhone,
                                                FirebaseAuthRequestDTO request) {
        if (claims.uid() != null && !claims.uid().isBlank()
                && (user.getFirebaseUid() == null || user.getFirebaseUid().isBlank())) {
            user.setFirebaseUid(claims.uid());
        }

        if (claims.provider() != null && !claims.provider().isBlank()
                && (user.getAuthProvider() == null || user.getAuthProvider().isBlank())) {
            user.setAuthProvider(claims.provider());
        }

        if (claims.emailVerified()) {
            user.setEmailVerified(true);
        }

        if ((user.getName() == null || user.getName().isBlank()) && resolvedName != null && !resolvedName.isBlank()) {
            user.setName(resolvedName);
        }

        if ((user.getProfilePictureUrl() == null || user.getProfilePictureUrl().isBlank())
                && resolvedPicture != null && !resolvedPicture.isBlank()) {
            user.setProfilePictureUrl(resolvedPicture.trim());
        }

        if ((user.getPhone() == null || user.getPhone().isBlank())
                && resolvedPhone != null && !userRepository.existsByPhone(resolvedPhone)) {
            user.setPhone(resolvedPhone);
        }

        if (request.role() != null) {
            user.setRole(request.role());
            user.setAccessLevel(request.role() == Role.ADMINISTRADOR ? "ROLE_ADMIN" : "ROLE_USER");
        }

        if (request.customRole() != null && !request.customRole().isBlank()) {
            user.setCustomRole(request.customRole().trim());
        }

        return userRepository.save(user);
    }

    private User createNewUserFromFirebase(FirebaseTokenClaims claims,
                                           String normalizedEmail,
                                           String resolvedName,
                                           String resolvedPicture,
                                           String resolvedPhone,
                                           FirebaseAuthRequestDTO request) {
        Role userRole = request.role() != null ? request.role() : Role.PROJETISTA;
        String accessLevel = (userRole == Role.ADMINISTRADOR) ? "ROLE_ADMIN" : "ROLE_USER";

        String phoneToSave = (resolvedPhone != null && !userRepository.existsByPhone(resolvedPhone))
                ? resolvedPhone
                : null;

        User newUser = User.builder()
                .name(resolvedName)
                .email(normalizedEmail)
                .phone(phoneToSave)
                .password("")
                .firebaseUid(claims.uid())
                .authProvider(claims.provider() != null ? claims.provider() : "FIREBASE_EMAIL")
                .emailVerified(claims.emailVerified())
                .profilePictureUrl(resolvedPicture != null ? resolvedPicture.trim() : null)
                .role(userRole)
                .customRole(request.customRole())
                .accessLevel(accessLevel)
                .build();

        return userRepository.save(newUser);
    }

    private FirebaseTokenClaims verifyOrDecodeFirebaseToken(FirebaseAuthRequestDTO request) {
        String idToken = request.idToken().trim();

        if (!FirebaseApp.getApps().isEmpty()) {
            try {
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
                String provider = extractProviderFromFirebaseMap(decodedToken.getClaims());
                return new FirebaseTokenClaims(
                        decodedToken.getUid(),
                        decodedToken.getEmail(),
                        decodedToken.getName(),
                        decodedToken.getPicture(),
                        decodedToken.isEmailVerified(),
                        provider
                );
            } catch (FirebaseAuthException e) {
                throw new BusinessException("Token do Firebase inválido ou expirado: " + e.getMessage());
            }
        }

        return decodeFirebaseJwtFallback(idToken);
    }

    private FirebaseTokenClaims decodeFirebaseJwtFallback(String idToken) {
        try {
            DecodedJWT jwt = JWT.decode(idToken);

            if (jwt.getExpiresAt() != null && jwt.getExpiresAt().toInstant().isBefore(Instant.now())) {
                throw new BusinessException("O token do Firebase expirou. Faça login novamente.");
            }

            String uid = jwt.getSubject();
            if (uid == null || uid.isBlank()) {
                uid = getStringClaim(jwt, "user_id");
            }

            String email = getStringClaim(jwt, "email");
            String name = getStringClaim(jwt, "name");
            String picture = getStringClaim(jwt, "picture");

            Claim emailVerifiedClaim = jwt.getClaim("email_verified");
            boolean emailVerified = (emailVerifiedClaim != null && !emailVerifiedClaim.isNull())
                    ? Boolean.TRUE.equals(emailVerifiedClaim.asBoolean())
                    : true;

            String provider = "FIREBASE_EMAIL";
            Claim firebaseClaim = jwt.getClaim("firebase");
            if (firebaseClaim != null && !firebaseClaim.isNull()) {
                Map<String, Object> firebaseMap = firebaseClaim.asMap();
                provider = extractProviderFromMap(firebaseMap);
            } else if (jwt.getIssuer() != null && jwt.getIssuer().contains("accounts.google.com")) {
                provider = "GOOGLE";
            }

            return new FirebaseTokenClaims(uid, email, name, picture, emailVerified, provider);
        } catch (JWTDecodeException ex) {
            throw new BusinessException("Token de autenticação do Firebase malformado ou inválido.");
        }
    }

    @SuppressWarnings("unchecked")
    private String extractProviderFromFirebaseMap(Map<String, Object> claims) {
        if (claims == null) {
            return "FIREBASE_EMAIL";
        }
        Object firebaseObj = claims.get("firebase");
        if (firebaseObj instanceof Map<?, ?> firebaseMap) {
            return extractProviderFromMap((Map<String, Object>) firebaseMap);
        }
        return "FIREBASE_EMAIL";
    }

    private String extractProviderFromMap(Map<String, Object> firebaseMap) {
        if (firebaseMap == null) {
            return "FIREBASE_EMAIL";
        }
        Object signInProvider = firebaseMap.get("sign_in_provider");
        if (signInProvider != null && "google.com".equalsIgnoreCase(signInProvider.toString())) {
            return "GOOGLE";
        }
        return "FIREBASE_EMAIL";
    }

    private String getStringClaim(DecodedJWT jwt, String claimName) {
        Claim claim = jwt.getClaim(claimName);
        return (claim != null && !claim.isNull()) ? claim.asString() : null;
    }

    private String resolveDisplayName(String tokenName, String requestName, String email) {
        if (tokenName != null && !tokenName.isBlank()) {
            return tokenName.trim();
        }
        if (requestName != null && !requestName.isBlank()) {
            return requestName.trim();
        }
        int atIndex = email.indexOf('@');
        if (atIndex > 0) {
            String prefix = email.substring(0, atIndex).replace('.', ' ').replace('_', ' ').trim();
            if (!prefix.isEmpty()) {
                return Character.toUpperCase(prefix.charAt(0)) + prefix.substring(1);
            }
        }
        return "Colaborador";
    }

    private LoginResponseDTO buildLoginResponse(User user) {
        String token = (jwtTokenProvider != null) ? jwtTokenProvider.generateToken(user) : "";
        String userRole = (user.getRole() != null) ? user.getRole().name() : user.getAccessLevel();

        return LoginResponseDTO.builder()
                .token(token)
                .type("Bearer")
                .email(user.getEmail())
                .name(user.getName())
                .role(userRole)
                .build();
    }

    private record FirebaseTokenClaims(
            String uid,
            String email,
            String name,
            String picture,
            boolean emailVerified,
            String provider
    ) {}
}