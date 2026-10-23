package com.ngo.ngoplatform.service;

import com.ngo.ngoplatform.dto.AuthRequest;
import com.ngo.ngoplatform.dto.AuthResponse;
import com.ngo.ngoplatform.dto.RegisterDonorRequest;
import com.ngo.ngoplatform.dto.RegisterNgoRequest;
import com.ngo.ngoplatform.entity.Donor;
import com.ngo.ngoplatform.entity.Ngo;
import com.ngo.ngoplatform.jwt.JwtService;
import com.ngo.ngoplatform.repository.DonorRepository;
import com.ngo.ngoplatform.repository.NgoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final NgoRepository ngoRepository;
    private final DonorRepository donorRepository;
    private final NgoService ngoService;
    private final DonorService donorService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse login(AuthRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String suppliedPassword = request.getPassword();

        Ngo ngo = ngoRepository.findByEmailIgnoreCase(email).orElse(null);
        if (ngo != null && passwordMatchesAndUpgrade(suppliedPassword, ngo.getPassword())) {
            // Legacy accounts stored plain-text passwords; upgrade after a successful login.
            if (!NgoService.isEncoded(ngo.getPassword())) {
                ngo.setPassword(passwordEncoder.encode(suppliedPassword));
                ngoRepository.save(ngo);
            }
            return new AuthResponse(jwtService.generateToken(ngo.getEmail()), "NGO", ngo.getId(), ngo.getName());
        }

        Donor donor = donorRepository.findByEmailIgnoreCase(email).orElse(null);
        if (donor != null && passwordMatchesAndUpgrade(suppliedPassword, donor.getPassword())) {
            if (!NgoService.isEncoded(donor.getPassword())) {
                donor.setPassword(passwordEncoder.encode(suppliedPassword));
                donorRepository.save(donor);
            }
            return new AuthResponse(jwtService.generateToken(donor.getEmail()), "DONOR", donor.getId(), donor.getName());
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

    @Transactional
    public AuthResponse registerNgo(RegisterNgoRequest request) {
        Ngo ngo = new Ngo();
        ngo.setName(request.getName().trim());
        ngo.setEmail(request.getEmail().trim().toLowerCase());
        ngo.setPassword(request.getPassword());
        ngo.setPhone(request.getPhone());
        ngo.setAddress(request.getAddress());
        ngo.setDescription(request.getDescription());
        Ngo saved = ngoService.saveNgo(ngo);
        return new AuthResponse(jwtService.generateToken(saved.getEmail()), "NGO", saved.getId(), saved.getName());
    }

    @Transactional
    public AuthResponse registerDonor(RegisterDonorRequest request) {
        Donor donor = new Donor();
        donor.setName(request.getName().trim());
        donor.setEmail(request.getEmail().trim().toLowerCase());
        donor.setPassword(request.getPassword());
        donor.setPhone(request.getPhone());
        donor.setAddress(request.getAddress());
        Donor saved = donorService.saveDonor(donor);
        return new AuthResponse(jwtService.generateToken(saved.getEmail()), "DONOR", saved.getId(), saved.getName());
    }

    private boolean passwordMatchesAndUpgrade(String supplied, String stored) {
        if (supplied == null || stored == null) return false;
        if (NgoService.isEncoded(stored)) {
            try {
                return passwordEncoder.matches(supplied, stored);
            } catch (IllegalArgumentException ignored) {
                return false;
            }
        }
        return stored.equals(supplied);
    }
}
