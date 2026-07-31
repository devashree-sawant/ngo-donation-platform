package com.ngo.ngoplatform.service;

import com.ngo.ngoplatform.dto.AuthRequest;
import com.ngo.ngoplatform.dto.AuthResponse;
import com.ngo.ngoplatform.entity.Donor;
import com.ngo.ngoplatform.entity.Ngo;
import com.ngo.ngoplatform.jwt.JwtService;
import com.ngo.ngoplatform.repository.DonorRepository;
import com.ngo.ngoplatform.repository.NgoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final NgoRepository ngoRepository;
    private final DonorRepository donorRepository;
    private final JwtService jwtService;

    public AuthResponse login(AuthRequest request) {

        Ngo ngo = ngoRepository.findByEmail(request.getEmail()).orElse(null);

        if (ngo != null && ngo.getPassword().equals(request.getPassword())) {
            return new AuthResponse(
                    jwtService.generateToken(ngo.getEmail()),
                    "NGO",
                    ngo.getId()
            );
        }

        Donor donor = donorRepository.findByEmail(request.getEmail()).orElse(null);

        if (donor != null && donor.getPassword().equals(request.getPassword())) {
            return new AuthResponse(
                    jwtService.generateToken(donor.getEmail()),
                    "DONOR",
                    donor.getId()
            );
        }

        throw new RuntimeException("Invalid Credentials");
    }
}