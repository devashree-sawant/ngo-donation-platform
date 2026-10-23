package com.ngo.ngoplatform.service;

import com.ngo.ngoplatform.entity.Need;
import com.ngo.ngoplatform.entity.Ngo;
import com.ngo.ngoplatform.repository.ContributionRepository;
import com.ngo.ngoplatform.repository.NeedRepository;
import com.ngo.ngoplatform.repository.NgoRepository;
import com.ngo.ngoplatform.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NeedService {

    private final NeedRepository needRepository;
    private final NgoRepository ngoRepository;
    private final ContributionRepository contributionRepository;
    private final CurrentUser currentUser;

    @Transactional
    public Need saveNeed(Need request) {
        currentUser.requireRole("NGO");
        Ngo owner = ngoRepository.findByEmailIgnoreCase(currentUser.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "NGO account not found"));

        Need need = new Need();
        need.setTitle(requireText(request.getTitle(), "Campaign title is required"));
        need.setDescription(request.getDescription());
        need.setNgo(owner);
        configureNeedType(need, request.getType(), request.getTargetAmount(), request.getItemRequired(), request.getRequiredQuantity(), true);
        refreshStatus(need);
        return needRepository.save(need);
    }

    @Transactional(readOnly = true)
    public List<Need> getAllNeeds() {
        return needRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Need getNeedById(Long id) {
        return findNeed(id);
    }

    @Transactional
    public Need updateNeed(Long id, Need request) {
        Need existing = findNeed(id);
        ensureOwner(existing);

        String requestedType = normalizeType(request.getType());
        boolean hasContributions = contributionRepository.existsByNeedId(id);
        if (!requestedType.equalsIgnoreCase(existing.getType()) && hasContributions) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Campaign type cannot be changed after contributions have been recorded");
        }
        if (requestedType.equalsIgnoreCase(existing.getType()) && "MONEY".equals(requestedType)
                && request.getTargetAmount() != null
                && request.getTargetAmount() < valueOrZero(existing.getCurrentAmount())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The target amount cannot be lower than the amount already raised");
        }
        if (requestedType.equalsIgnoreCase(existing.getType()) && "ITEM".equals(requestedType)
                && request.getRequiredQuantity() != null
                && request.getRequiredQuantity() < integerOrZero(existing.getReceivedQuantity())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The required quantity cannot be lower than the quantity already received");
        }

        existing.setTitle(requireText(request.getTitle(), "Campaign title is required"));
        existing.setDescription(request.getDescription());
        configureNeedType(existing, requestedType, request.getTargetAmount(), request.getItemRequired(), request.getRequiredQuantity(), false);
        refreshStatus(existing);
        return needRepository.save(existing);
    }

    @Transactional
    public void deleteNeed(Long id) {
        Need need = findNeed(id);
        ensureOwner(need);
        if (contributionRepository.existsByNeedId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This campaign has contribution history and cannot be deleted. Keep it for donor records.");
        }
        needRepository.delete(need);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getNeedProgress(Long id) {
        Need need = findNeed(id);
        Map<String, Object> progress = new HashMap<>();
        double percentage;

        if ("MONEY".equalsIgnoreCase(need.getType())) {
            double current = valueOrZero(need.getCurrentAmount());
            double target = valueOrZero(need.getTargetAmount());
            percentage = target > 0 ? (current / target) * 100.0 : 0.0;
            progress.put("currentAmount", current);
            progress.put("targetAmount", target);
            progress.put("currency", "INR");
        } else {
            int received = integerOrZero(need.getReceivedQuantity());
            int required = integerOrZero(need.getRequiredQuantity());
            percentage = required > 0 ? ((double) received / required) * 100.0 : 0.0;
            progress.put("receivedQuantity", received);
            progress.put("requiredQuantity", required);
            progress.put("itemRequired", need.getItemRequired());
        }

        percentage = Math.max(0.0, Math.min(100.0, percentage));
        progress.put("needId", need.getId());
        progress.put("type", need.getType());
        progress.put("percentage", Math.round(percentage * 100.0) / 100.0);
        progress.put("status", need.getStatus());
        return progress;
    }

    private Need findNeed(Long id) {
        return needRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));
    }

    private void ensureOwner(Need need) {
        currentUser.requireRole("NGO");
        if (need.getNgo() == null || !need.getNgo().getEmail().equalsIgnoreCase(currentUser.email())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only manage your own campaigns");
        }
    }

    private void configureNeedType(Need need, String type, Double targetAmount,
                                   String itemRequired, Integer requiredQuantity, boolean creating) {
        String normalized = normalizeType(type);
        need.setType(normalized);

        if ("MONEY".equals(normalized)) {
            if (targetAmount == null || !Double.isFinite(targetAmount) || targetAmount <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Money campaigns need a target amount greater than zero");
            }
            need.setTargetAmount(targetAmount);
            need.setItemRequired(null);
            need.setRequiredQuantity(null);
            need.setReceivedQuantity(0);
            if (creating) need.setCurrentAmount(0.0);
            else if (need.getCurrentAmount() == null) need.setCurrentAmount(0.0);
        } else {
            if (itemRequired == null || itemRequired.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item campaigns need an item name");
            }
            if (requiredQuantity == null || requiredQuantity <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Required quantity must be greater than zero");
            }
            need.setTargetAmount(null);
            need.setCurrentAmount(null);
            need.setItemRequired(itemRequired.trim());
            need.setRequiredQuantity(requiredQuantity);
            if (creating) need.setReceivedQuantity(0);
            else if (need.getReceivedQuantity() == null) need.setReceivedQuantity(0);
        }
    }

    private String normalizeType(String type) {
        if (type == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Need type is required");
        String normalized = type.trim().toUpperCase();
        if (!normalized.equals("MONEY") && !normalized.equals("ITEM")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Need type must be MONEY or ITEM");
        }
        return normalized;
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        return value.trim();
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
