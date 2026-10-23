package com.ngo.ngoplatform.controller;

import com.ngo.ngoplatform.dto.AuthRequest;
import com.ngo.ngoplatform.dto.AuthResponse;
import com.ngo.ngoplatform.dto.RegisterDonorRequest;
import com.ngo.ngoplatform.dto.RegisterNgoRequest;
import com.ngo.ngoplatform.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register/ngo")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registerNgo(@Valid @RequestBody RegisterNgoRequest request) {
        return authService.registerNgo(request);
    }

    @PostMapping("/register/donor")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registerDonor(@Valid @RequestBody RegisterDonorRequest request) {
        return authService.registerDonor(request);
    }
}
