package com.ngo.ngoplatform.dto;

import java.time.LocalDate;

public record CertificateResponse(
        Long contributionId,
        String donorName,
        String campaignTitle,
        String ngoName,
        String type,
        Double amount,
        String itemName,
        Integer quantity,
        LocalDate donationDate,
        String certificateTitle,
        String certificateMessage
) {}
