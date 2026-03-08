package com.example.Spring_traning.service;

import com.example.Spring_traning.dto.JwtResponseDto;
import com.example.Spring_traning.dto.LoginDto;
import com.example.Spring_traning.dto.UserRegistrationDto;
import com.example.Spring_traning.exception.ResourceNotFoundException;
import com.example.Spring_traning.repository.UserRepository;
import com.example.Spring_traning.security.JwtTokenProvider;
import com.example.Spring_traning.entity.Role;
import com.example.Spring_traning.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public JwtResponseDto login(LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return new JwtResponseDto(jwt, "Bearer", user.getId(), user.getEmail(), user.getRole().name());
    }

    public JwtResponseDto register(UserRegistrationDto registrationDto) {
        // Check if email already exists
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create new user account
        User user = new User();
        user.setFirstName(registrationDto.getFirstName());
        user.setLastName(registrationDto.getLastName());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setRole(Role.USER); // Default role

        User savedUser = userRepository.save(user);

        // Authenticate and generate token
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(registrationDto.getEmail(), registrationDto.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        return new JwtResponseDto(jwt, "Bearer", savedUser.getId(), savedUser.getEmail(), savedUser.getRole().name());
    }
}
