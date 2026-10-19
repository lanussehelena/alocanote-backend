package com.alocanote.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final com.alocanote.api.service.AuthService authService;

    public SecurityFilter(JwtTokenProvider jwtTokenProvider, UserDetailsServiceImpl userDetailsService) {
        this(jwtTokenProvider, userDetailsService, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public SecurityFilter(JwtTokenProvider jwtTokenProvider,
                          UserDetailsServiceImpl userDetailsService,
                          @org.springframework.context.annotation.Lazy com.alocanote.api.service.AuthService authService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = recoverToken(request);

        if (token != null) {
            String subject = jwtTokenProvider.validateToken(token);
            if (subject == null && authService != null) {
                subject = authService.resolveUserFromFirebaseToken(token)
                        .map(com.alocanote.api.model.entity.User::getEmail)
                        .orElse(null);
            }
            if (subject != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails;
                try {
                    userDetails = userDetailsService.loadUserByUsername(subject);
                } catch (org.springframework.security.core.userdetails.UsernameNotFoundException ex) {
                    String name = jwtTokenProvider.getClaim(token, "name");
                    String role = jwtTokenProvider.getClaim(token, "role");
                    String accessLevel = jwtTokenProvider.getClaim(token, "accessLevel");
                    userDetails = userDetailsService.loadOrRecoverUserFromToken(subject, name, role, accessLevel);
                }
                if (userDetails != null) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String candidate = authHeader.substring(7).trim();
            if (!candidate.isEmpty()
                    && !"null".equalsIgnoreCase(candidate)
                    && !"undefined".equalsIgnoreCase(candidate)) {
                return candidate;
            }
        }
        return null;
    }
}
