package com.ngo.ngoplatform.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "ngos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ngo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "NGO name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    @Column(nullable = false, unique = true)
    private String email;

    @Size(min = 8, message = "Password must contain at least 8 characters")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    private String phone;
    private String address;

    @Column(length = 1000)
    private String description;

    @OneToMany(mappedBy = "ngo", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Need> needs;
}
