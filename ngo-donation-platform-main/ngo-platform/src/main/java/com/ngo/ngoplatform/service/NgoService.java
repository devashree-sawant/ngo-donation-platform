package com.ngo.ngoplatform.service;

import com.ngo.ngoplatform.entity.Ngo;
import com.ngo.ngoplatform.repository.DonorRepository;
import com.ngo.ngoplatform.repository.NeedRepository;
import com.ngo.ngoplatform.repository.NgoRepository;
import com.ngo.ngoplatform.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NgoService {

    private final NgoRepository ngoRepository;
    private final DonorRepository donorRepository;
    private final NeedRepository needRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUser currentUser;

    @Transactional
    public Ngo saveNgo(Ngo ngo) {
        String email = normalizeEmail(ngo.getEmail());
        ensureEmailAvailable(email, null);
        ngo.setEmail(email);
        ngo.setPassword(encodeIfNeeded(ngo.getPassword()));
        return ngoRepository.save(ngo);
    }

    @Transactional(readOnly = true)
    public List<Ngo> getAllNgos() {
        return ngoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Ngo> getNgoById(Long id) {
        return ngoRepository.findById(id);
    }

    @Transactional
    public Ngo updateNgo(Long id, Ngo request) {
        Ngo existing = ngoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "NGO not found"));

        currentUser.requireRole("NGO");
        if (!existing.getEmail().equalsIgnoreCase(currentUser.email())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own NGO profile");
        }

        String email = normalizeEmail(request.getEmail());
        ensureEmailAvailable(email, existing.getId());
        existing.setName(request.getName());
        existing.setEmail(email);
        existing.setPhone(request.getPhone());
        existing.setAddress(request.getAddress());
        existing.setDescription(request.getDescription());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            existing.setPassword(encodeIfNeeded(request.getPassword()));
        }
        return ngoRepository.save(existing);
    }

    @Transactional
    public void deleteNgo(Long id) {
        Ngo existing = ngoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "NGO not found"));
        currentUser.requireRole("NGO");
        if (!existing.getEmail().equalsIgnoreCase(currentUser.email())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own NGO account");
        }
        if (needRepository.existsByNgoId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Delete this NGO's campaigns first. Campaigns with contributions must be retained for history.");
        }
        ngoRepository.delete(existing);
    }

    public String encodeIfNeeded(String password) {
        if (password == null || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }
        if (isEncoded(password)) return password;
        return passwordEncoder.encode(password);
    }

    private void ensureEmailAvailable(String email, Long currentId) {
        boolean ngoDuplicate = ngoRepository.findByEmailIgnoreCase(email)
                .filter(found -> currentId == null || !found.getId().equals(currentId)).isPresent();
        boolean donorDuplicate = donorRepository.existsByEmailIgnoreCase(email);
        if (ngoDuplicate || donorDuplicate) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        return email.trim().toLowerCase();
    }

    static boolean isEncoded(String password) {
        return password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$");
    }
}
