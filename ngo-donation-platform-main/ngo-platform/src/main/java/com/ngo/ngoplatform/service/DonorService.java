package com.ngo.ngoplatform.service;

import com.ngo.ngoplatform.entity.Donor;
import com.ngo.ngoplatform.repository.ContributionRepository;
import com.ngo.ngoplatform.repository.DonorRepository;
import com.ngo.ngoplatform.repository.NgoRepository;
import com.ngo.ngoplatform.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DonorService {

    private final DonorRepository donorRepository;
    private final NgoRepository ngoRepository;
    private final ContributionRepository contributionRepository;
    private final NgoService ngoService;
    private final CurrentUser currentUser;

    @Transactional
    public Donor saveDonor(Donor donor) {
        String email = normalizeEmail(donor.getEmail());
        ensureEmailAvailable(email, null);
        donor.setEmail(email);
        donor.setPassword(ngoService.encodeIfNeeded(donor.getPassword()));
        return donorRepository.save(donor);
    }

    @Transactional(readOnly = true)
    public List<Donor> getAllDonors() {
        return donorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Donor getDonorById(Long id) {
        Donor donor = donorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Donor not found"));
        if (!donor.getEmail().equalsIgnoreCase(currentUser.email()) && !currentUser.hasRole("NGO")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only view your own donor profile");
        }
        return donor;
    }

    @Transactional
    public Donor updateDonor(Long id, Donor request) {
        Donor existing = donorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Donor not found"));
        currentUser.requireRole("DONOR");
        if (!existing.getEmail().equalsIgnoreCase(currentUser.email())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own donor profile");
        }

        String email = normalizeEmail(request.getEmail());
        ensureEmailAvailable(email, existing.getId());
        existing.setName(request.getName());
        existing.setEmail(email);
        existing.setPhone(request.getPhone());
        existing.setAddress(request.getAddress());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            existing.setPassword(ngoService.encodeIfNeeded(request.getPassword()));
        }
        return donorRepository.save(existing);
    }

    @Transactional
    public void deleteDonor(Long id) {
        Donor donor = donorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Donor not found"));
        currentUser.requireRole("DONOR");
        if (!donor.getEmail().equalsIgnoreCase(currentUser.email())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own donor account");
        }
        if (contributionRepository.existsByDonorId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This donor has contribution history. Keep the account so donation records remain available.");
        }
        donorRepository.delete(donor);
    }

    private void ensureEmailAvailable(String email, Long currentId) {
        boolean donorDuplicate = donorRepository.findByEmailIgnoreCase(email)
                .filter(found -> currentId == null || !found.getId().equals(currentId)).isPresent();
        boolean ngoDuplicate = ngoRepository.existsByEmailIgnoreCase(email);
        if (donorDuplicate || ngoDuplicate) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        return email.trim().toLowerCase();
    }
}
