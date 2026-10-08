package com.mymart.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.mymart.config.FirebaseConfig;
import com.mymart.model.User;
import com.mymart.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthFilter extends OncePerRequestFilter {

    private final FirebaseConfig firebaseConfig;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractToken(request);
            if (StringUtils.hasText(token)) {
                UserPrincipal principal = null;

                // 1. Try Firebase Admin verification if Firebase is initialized
                if (firebaseConfig.isInitialized()) {
                    try {
                        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
                        String uid = decodedToken.getUid();
                        String email = decodedToken.getEmail();
                        String name = decodedToken.getName();

                        User user = userRepository.findByFirebaseUid(uid)
                                .orElseGet(() -> {
                                    // Automatically register/sync user if not in MongoDB yet
                                    User newUser = User.builder()
                                            .firebaseUid(uid)
                                            .email(email != null ? email.toLowerCase() : uid + "@firebase.user")
                                            .username(email != null ? email.split("@")[0].toLowerCase() : uid)
                                            .fullName(name != null ? name : "Customer")
                                            .role("ROLE_CUSTOMER")
                                            .createdAt(Instant.now())
                                            .build();
                                    return userRepository.save(newUser);
                                });

                        principal = UserPrincipal.create(user);
                    } catch (Exception fe) {
                        log.debug("Firebase ID token verification failed, checking local JWT fallback: {}", fe.getMessage());
                    }
                }

                // 2. If Firebase did not verify or is not initialized, check local JWT
                if (principal == null && jwtTokenProvider.validateToken(token)) {
                    Claims claims = jwtTokenProvider.getClaimsFromToken(token);
                    String userId = claims.getSubject();
                    User user = userRepository.findById(userId).orElse(null);
                    if (user != null) {
                        principal = UserPrincipal.create(user);
                    }
                }

                // 3. Set authentication in SecurityContext
                if (principal != null) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
