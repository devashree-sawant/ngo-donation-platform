package com.ngo.ngoplatform.service;

import com.ngo.ngoplatform.dto.CertificateResponse;
import com.ngo.ngoplatform.entity.Contribution;
import com.ngo.ngoplatform.entity.Donor;
import com.ngo.ngoplatform.entity.Need;
import com.ngo.ngoplatform.repository.ContributionRepository;
import com.ngo.ngoplatform.repository.DonorRepository;
import com.ngo.ngoplatform.repository.NeedRepository;
import com.ngo.ngoplatform.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContributionService {

    private final ContributionRepository contributionRepository;
    private final NeedRepository needRepository;
    private final DonorRepository donorRepository;
    private final CurrentUser currentUser;

    @Transactional
    public Contribution saveContribution(Contribution request) {
        currentUser.requireRole("DONOR");
        Donor donor = donorRepository.findByEmailIgnoreCase(currentUser.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Donor account not found"));

        if (request.getNeed() == null || request.getNeed().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campaign id is required");
        }
        Need need = needRepository.findByIdForUpdate(request.getNeed().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));
        if (request.getDonor() != null && request.getDonor().getId() != null
                && !request.getDonor().getId().equals(donor.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot donate using another donor's account");
        }
        if ("COMPLETED".equalsIgnoreCase(need.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This campaign is already completed");
        }

        String type = normalizeType(request.getType());
        Contribution contribution = new Contribution();
        contribution.setType(type);
        contribution.setDonor(donor);
        contribution.setNeed(need);
        contribution.setDonationDate(LocalDate.now());

        if ("MONEY".equals(type)) {
            if (!"MONEY".equalsIgnoreCase(need.getType())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This is an item campaign; submit an item contribution instead");
            }
            double amount = request.getAmount() == null ? 0 : request.getAmount();
            if (!Double.isFinite(amount) || amount <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Donation amount must be greater than zero");
            }
            double current = valueOrZero(need.getCurrentAmount());
            double target = valueOrZero(need.getTargetAmount());
            double remaining = target - current;
            if (amount > remaining + 0.000001) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Donation exceeds the remaining campaign goal of ₹" + String.format("%.2f", Math.max(0, remaining)));
            }
            contribution.setAmount(amount);
            contribution.setItemName(null);
            contribution.setQuantity(null);
            need.setCurrentAmount(current + amount);
        } else {
            if (!"ITEM".equalsIgnoreCase(need.getType())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This is a money campaign; submit a money contribution instead");
            }
            int quantity = request.getQuantity() == null ? 0 : request.getQuantity();
            if (quantity <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item quantity must be greater than zero");
            }
            String canonicalItem = need.getItemRequired();
            if (request.getItemName() != null && !request.getItemName().isBlank()
                    && !request.getItemName().trim().equalsIgnoreCase(canonicalItem)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This campaign accepts: " + canonicalItem);
            }
            int received = integerOrZero(need.getReceivedQuantity());
            int required = integerOrZero(need.getRequiredQuantity());
            int remaining = required - received;
            if (quantity > remaining) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Quantity exceeds the remaining requirement of " + Math.max(0, remaining));
            }
            contribution.setAmount(null);
            contribution.setItemName(canonicalItem);
            contribution.setQuantity(quantity);
            need.setReceivedQuantity(received + quantity);
        }

        refreshStatus(need);
        needRepository.save(need);
        return contributionRepository.save(contribution);
    }

    @Transactional(readOnly = true)
    public List<Contribution> getAllContributions() {
        currentUser.requireRole("NGO");
        return contributionRepository.findAll().stream()
                .filter(c -> c.getNeed() != null && c.getNeed().getNgo() != null
                        && c.getNeed().getNgo().getEmail().equalsIgnoreCase(currentUser.email()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Contribution getContributionById(Long id) {
        Contribution contribution = findContribution(id);
        if (!canView(contribution)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access this contribution");
        }
        return contribution;
    }

    @Transactional(readOnly = true)
    public List<Contribution> getContributionsByDonorId(Long donorId) {
        currentUser.requireRole("DONOR");
        Donor donor = donorRepository.findByEmailIgnoreCase(currentUser.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Donor account not found"));
        if (!donor.getId().equals(donorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only view your own contribution history");
        }
        return contributionRepository.findByDonorIdOrderByDonationDateDescIdDesc(donorId);
    }

    @Transactional(readOnly = true)
    public CertificateResponse getCertificate(Long id) {
        Contribution contribution = findContribution(id);
        currentUser.requireRole("DONOR");
        if (contribution.getDonor() == null
                || !contribution.getDonor().getEmail().equalsIgnoreCase(currentUser.email())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only generate a certificate for your own contribution");
        }
        Need need = contribution.getNeed();
        String type = contribution.getType();
        return new CertificateResponse(
                contribution.getId(),
                contribution.getDonor().getName(),
                need.getTitle(),
                need.getNgo() == null ? "NGO Donation Platform" : need.getNgo().getName(),
                type,
                contribution.getAmount(),
                contribution.getItemName(),
                contribution.getQuantity(),
                contribution.getDonationDate(),
                "Donation Acknowledgement Certificate",
                "This certificate acknowledges the contribution recorded by NGO Donation Platform. It is not a tax exemption certificate."
        );
    }

    @Transactional
    public void deleteContribution(Long id) {
        Contribution contribution = findContribution(id);
        if (!canView(contribution)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot delete this contribution");
        }
        Long needId = contribution.getNeed().getId();
        Need need = needRepository.findByIdForUpdate(needId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));
        if ("MONEY".equalsIgnoreCase(contribution.getType())) {
            need.setCurrentAmount(Math.max(0.0, valueOrZero(need.getCurrentAmount()) - valueOrZero(contribution.getAmount())));
        } else {
            need.setReceivedQuantity(Math.max(0, integerOrZero(need.getReceivedQuantity()) - integerOrZero(contribution.getQuantity())));
        }
        refreshStatus(need);
        needRepository.save(need);
        contributionRepository.delete(contribution);
    }

    private Contribution findContribution(Long id) {
        return contributionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contribution not found"));
    }

    private boolean canView(Contribution contribution) {
        if (currentUser.hasRole("DONOR") && contribution.getDonor() != null
                && contribution.getDonor().getEmail().equalsIgnoreCase(currentUser.email())) return true;
        return currentUser.hasRole("NGO") && contribution.getNeed() != null
                && contribution.getNeed().getNgo() != null
                && contribution.getNeed().getNgo().getEmail().equalsIgnoreCase(currentUser.email());
    }

    private String normalizeType(String type) {
        if (type == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contribution type is required");
        String normalized = type.trim().toUpperCase();
        if (!normalized.equals("MONEY") && !normalized.equals("ITEM")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contribution type must be MONEY or ITEM");
        }
        return normalized;
    }

    private void refreshStatus(Need need) {
        if ("MONEY".equalsIgnoreCase(need.getType())) {
            need.setStatus(valueOrZero(need.getCurrentAmount()) >= valueOrZero(need.getTargetAmount()) ? "COMPLETED" : "PENDING");
        } else {
            need.setStatus(integerOrZero(need.getReceivedQuantity()) >= integerOrZero(need.getRequiredQuantity()) ? "COMPLETED" : "PENDING");
        }
    }

    private double valueOrZero(Double value) { return value == null ? 0.0 : value; }
    private int integerOrZero(Integer value) { return value == null ? 0 : value; }
}
