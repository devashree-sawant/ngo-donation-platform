package com.ngo.ngoplatform.controller;

import com.ngo.ngoplatform.entity.Contribution;
import com.ngo.ngoplatform.entity.Donor;
import com.ngo.ngoplatform.service.ContributionService;
import com.ngo.ngoplatform.service.DonorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/donors")
@RequiredArgsConstructor
public class DonorController {

    private final DonorService donorService;
    private final ContributionService contributionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Donor createDonor(@Valid @RequestBody Donor donor) {
        return donorService.saveDonor(donor);
    }

    @GetMapping
    public List<Donor> getAllDonors() {
        return donorService.getAllDonors();
    }

    @GetMapping("/{id}")
    public Donor getDonorById(@PathVariable Long id) {
        return donorService.getDonorById(id);
    }

    @PutMapping("/{id}")
    public Donor updateDonor(@PathVariable Long id, @Valid @RequestBody Donor donor) {
        return donorService.updateDonor(id, donor);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteDonor(@PathVariable Long id) {
        donorService.deleteDonor(id);
        return Map.of("message", "Donor account deleted successfully");
    }

    @GetMapping("/{id}/contributions")
    public List<Contribution> getDonorContributions(@PathVariable Long id) {
        return contributionService.getContributionsByDonorId(id);
    }
}
