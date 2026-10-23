package com.ngo.ngoplatform.controller;

import com.ngo.ngoplatform.dto.CertificateResponse;
import com.ngo.ngoplatform.entity.Contribution;
import com.ngo.ngoplatform.service.ContributionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contributions")
@RequiredArgsConstructor
public class ContributionController {

    private final ContributionService contributionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Contribution createContribution(@Valid @RequestBody Contribution contribution) {
        return contributionService.saveContribution(contribution);
    }

    @GetMapping
    public List<Contribution> getAllContributions() {
        return contributionService.getAllContributions();
    }

    @GetMapping("/{id}")
    public Contribution getContributionById(@PathVariable Long id) {
        return contributionService.getContributionById(id);
    }

    @GetMapping("/{id}/certificate")
    public CertificateResponse getCertificate(@PathVariable Long id) {
        return contributionService.getCertificate(id);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteContribution(@PathVariable Long id) {
        contributionService.deleteContribution(id);
        return Map.of("message", "Contribution deleted and campaign progress recalculated");
    }
}
