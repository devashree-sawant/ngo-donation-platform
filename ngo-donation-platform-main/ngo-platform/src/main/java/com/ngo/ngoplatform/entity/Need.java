package com.ngo.ngoplatform.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "needs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Need {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Campaign title is required")
    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @NotBlank(message = "Need type is required")
    @Pattern(regexp = "(?i)MONEY|ITEM", message = "Need type must be MONEY or ITEM")
    @Column(nullable = false)
    private String type;

    @Positive(message = "Target amount must be greater than zero")
    private Double targetAmount;

    private Double currentAmount = 0.0;
    private String itemRequired;

    @Positive(message = "Required quantity must be greater than zero")
    private Integer requiredQuantity;

    private Integer receivedQuantity = 0;
    private String status = "PENDING";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ngo_id", nullable = false)
    private Ngo ngo;

    @OneToMany(mappedBy = "need", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Contribution> contributions;
}
