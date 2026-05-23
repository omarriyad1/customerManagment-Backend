package org.task.customermanagment.Service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.task.customermanagment.Dto.AuthRequest;
import org.task.customermanagment.Dto.AuthResponse;
import org.task.customermanagment.Dto.RegisterRequest;
import org.task.customermanagment.Exception.UserAlreadyExistsException;
import org.task.customermanagment.Model.AppUser;
import org.task.customermanagment.Repository.UserRepository;
import org.task.customermanagment.security.JwtUtil;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        log.info("Registering user: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException(
                    "Username '" + request.getUsername() + "' is already taken"
            );
        }

        String role = (request.getRole() != null && !request.getRole().isBlank())
                ? normalizeRole(request.getRole())
                : "ROLE_USER";

        AppUser newUser = AppUser.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        userRepository.save(newUser);
        log.info("User registered with username: {} and role: {}", newUser.getUsername(), role);

        UserDetails userDetails = userDetailsService.loadUserByUsername(newUser.getUsername());
        String token = jwtUtil.generateToken(userDetails);

        return new AuthResponse(token, newUser.getUsername(), jwtUtil.getExpiration());
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()
                )
        );
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtUtil.generateToken(userDetails);
        return new AuthResponse(token, userDetails.getUsername(), jwtUtil.getExpiration());
    }

    private String normalizeRole(String role) {
        String upper = role.toUpperCase();
        return upper.startsWith("ROLE_") ? upper : "ROLE_" + upper;
    }
}