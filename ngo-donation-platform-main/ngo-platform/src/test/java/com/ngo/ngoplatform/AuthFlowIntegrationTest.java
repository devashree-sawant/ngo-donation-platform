package com.ngo.ngoplatform;

import com.ngo.ngoplatform.dto.AuthRequest;
import com.ngo.ngoplatform.dto.AuthResponse;
import com.ngo.ngoplatform.dto.RegisterDonorRequest;
import com.ngo.ngoplatform.repository.ContributionRepository;
import com.ngo.ngoplatform.repository.DonorRepository;
import com.ngo.ngoplatform.repository.NeedRepository;
import com.ngo.ngoplatform.repository.NgoRepository;
import com.ngo.ngoplatform.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class AuthFlowIntegrationTest {

    @Autowired private AuthService authService;
    @Autowired private NgoRepository ngoRepository;
    @Autowired private DonorRepository donorRepository;
    @Autowired private NeedRepository needRepository;
    @Autowired private ContributionRepository contributionRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        contributionRepository.deleteAll();
        needRepository.deleteAll();
        donorRepository.deleteAll();
        ngoRepository.deleteAll();
    }

    @Test
    void registrationHashesPasswordAndReturnsLoginDetails() {
        RegisterDonorRequest request = new RegisterDonorRequest();
        request.setName("New Donor");
        request.setEmail("new-donor@example.com");
        request.setPassword("safePassword123");

        AuthResponse response = authService.registerDonor(request);
        var saved = donorRepository.findByEmailIgnoreCase(request.getEmail()).orElseThrow();

        assertEquals("DONOR", response.getRole());
        assertNotNull(response.getToken());
        assertNotNull(response.getId());
        assertNotEquals(request.getPassword(), saved.getPassword());
        assertTrue(passwordEncoder.matches(request.getPassword(), saved.getPassword()));
    }

    @Test
    void successfulLegacyPlaintextLoginUpgradesPasswordHash() {
        com.ngo.ngoplatform.entity.Ngo ngo = new com.ngo.ngoplatform.entity.Ngo();
        ngo.setName("Legacy NGO");
        ngo.setEmail("legacy-ngo@example.com");
        ngo.setPassword("legacyPassword123");
        ngo = ngoRepository.save(ngo);

        AuthRequest request = new AuthRequest();
        request.setEmail(ngo.getEmail());
        request.setPassword("legacyPassword123");

        AuthResponse response = authService.login(request);
        var saved = ngoRepository.findById(ngo.getId()).orElseThrow();

        assertEquals("NGO", response.getRole());
        assertEquals(ngo.getId(), response.getId());
        assertNotEquals("legacyPassword123", saved.getPassword());
        assertTrue(passwordEncoder.matches("legacyPassword123", saved.getPassword()));
    }
}
