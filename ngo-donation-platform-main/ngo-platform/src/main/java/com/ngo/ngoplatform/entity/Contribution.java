package com.ngo.ngoplatform.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "contributions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Contribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Pattern(regexp = "(?i)MONEY|ITEM", message = "Contribution type must be MONEY or ITEM")
    @Column(nullable = false)
    private String type;

    @Positive(message = "Donation amount must be greater than zero")
    private Double amount;

    private String itemName;

    @Positive(message = "Donation quantity must be greater than zero")
    private Integer quantity;

    private LocalDate donationDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "donor_id", nullable = false)
    private Donor donor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "need_id", nullable = false)
    private Need need;
}
