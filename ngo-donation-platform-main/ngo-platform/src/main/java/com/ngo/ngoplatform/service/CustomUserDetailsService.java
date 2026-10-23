package com.ngo.ngoplatform.service;

import com.ngo.ngoplatform.entity.Donor;
import com.ngo.ngoplatform.entity.Ngo;
import com.ngo.ngoplatform.repository.DonorRepository;
import com.ngo.ngoplatform.repository.NgoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final NgoRepository ngoRepository;
    private final DonorRepository donorRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Ngo ngo = ngoRepository.findByEmailIgnoreCase(email).orElse(null);
        if (ngo != null) {
            return new User(ngo.getEmail(), ngo.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_NGO")));
        }

        Donor donor = donorRepository.findByEmailIgnoreCase(email).orElse(null);
        if (donor != null) {
            return new User(donor.getEmail(), donor.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_DONOR")));
        }
        throw new UsernameNotFoundException("Account not found");
    }
}
