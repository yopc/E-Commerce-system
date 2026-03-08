package com.example.Spring_traning.controller;

import com.example.Spring_traning.dto.JwtResponseDto;
import com.example.Spring_traning.dto.LoginDto;
import com.example.Spring_traning.dto.UserRegistrationDto;
import com.example.Spring_traning.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> login(@Valid @RequestBody LoginDto loginDto) {
        JwtResponseDto response = authService.login(loginDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<JwtResponseDto> register(@Valid @RequestBody UserRegistrationDto registrationDto) {
        JwtResponseDto response = authService.register(registrationDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
