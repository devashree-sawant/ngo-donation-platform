package com.ngo.ngoplatform;

import com.ngo.ngoplatform.entity.Contribution;
import com.ngo.ngoplatform.entity.Donor;
import com.ngo.ngoplatform.entity.Need;
import com.ngo.ngoplatform.entity.Ngo;
import com.ngo.ngoplatform.repository.ContributionRepository;
import com.ngo.ngoplatform.repository.DonorRepository;
import com.ngo.ngoplatform.repository.NeedRepository;
import com.ngo.ngoplatform.repository.NgoRepository;
import com.ngo.ngoplatform.service.ContributionService;
import com.ngo.ngoplatform.service.NeedService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class ContributionFlowIntegrationTest {

    @Autowired private ContributionService contributionService;
    @Autowired private NeedService needService;
    @Autowired private NeedRepository needRepository;
    @Autowired private ContributionRepository contributionRepository;
    @Autowired private NgoRepository ngoRepository;
    @Autowired private DonorRepository donorRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Ngo ngo;
    private Donor donor;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        contributionRepository.deleteAll();
        needRepository.deleteAll();
        donorRepository.deleteAll();
        ngoRepository.deleteAll();

        ngo = new Ngo();
        ngo.setName("Test NGO");
        ngo.setEmail("test-ngo@example.com");
        ngo.setPassword(passwordEncoder.encode("password123"));
        ngo = ngoRepository.save(ngo);

        donor = new Donor();
        donor.setName("Test Donor");
        donor.setEmail("test-donor@example.com");
        donor.setPassword(passwordEncoder.encode("password123"));
        donor = donorRepository.save(donor);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void moneyContributionUpdatesAmountAndCompletionStatus() {
        authenticate(ngo.getEmail(), "NGO");
        Need request = new Need();
        request.setTitle("Education Fund");
        request.setDescription("Test campaign");
        request.setType("MONEY");
        request.setTargetAmount(1000.0);
        Need need = needService.saveNeed(request);

        authenticate(donor.getEmail(), "DONOR");
        Contribution requestContribution = new Contribution();
        requestContribution.setType("MONEY");
        requestContribution.setAmount(1000.0);
        Need needReference = new Need();
        needReference.setId(need.getId());
        requestContribution.setNeed(needReference);

        Contribution saved = contributionService.saveContribution(requestContribution);
        Need updated = needRepository.findById(need.getId()).orElseThrow();

        assertNotNull(saved.getId());
        assertEquals(1000.0, updated.getCurrentAmount(), 0.001);
        assertEquals("COMPLETED", updated.getStatus());
    }

    @Test
    void itemContributionUpdatesQuantityAndDeletionReversesIt() {
        authenticate(ngo.getEmail(), "NGO");
        Need request = new Need();
        request.setTitle("Food Kits");
        request.setDescription("Test item campaign");
        request.setType("ITEM");
        request.setItemRequired("Food Kit");
        request.setRequiredQuantity(4);
        Need need = needService.saveNeed(request);

        authenticate(donor.getEmail(), "DONOR");
        Contribution requestContribution = new Contribution();
        requestContribution.setType("ITEM");
        requestContribution.setItemName("Food Kit");
        requestContribution.setQuantity(4);
        Need needReference = new Need();
        needReference.setId(need.getId());
        requestContribution.setNeed(needReference);

        Contribution saved = contributionService.saveContribution(requestContribution);
        Need completed = needRepository.findById(need.getId()).orElseThrow();
        assertEquals(Integer.valueOf(4), completed.getReceivedQuantity());
        assertEquals("COMPLETED", completed.getStatus());

        contributionService.deleteContribution(saved.getId());
        Need reopened = needRepository.findById(need.getId()).orElseThrow();
        assertEquals(Integer.valueOf(0), reopened.getReceivedQuantity());
        assertEquals("PENDING", reopened.getStatus());
    }


    @Test
    void donorHistoryAndCertificateReturnRecordedContribution() {
        authenticate(ngo.getEmail(), "NGO");
        Need request = new Need();
        request.setTitle("Scholarship Fund");
        request.setDescription("Test acknowledgement workflow");
        request.setType("MONEY");
        request.setTargetAmount(500.0);
        Need need = needService.saveNeed(request);

        authenticate(donor.getEmail(), "DONOR");
        Contribution requestContribution = new Contribution();
        requestContribution.setType("MONEY");
        requestContribution.setAmount(125.0);
        Need needReference = new Need();
        needReference.setId(need.getId());
        requestContribution.setNeed(needReference);
        Contribution saved = contributionService.saveContribution(requestContribution);

        var history = contributionService.getContributionsByDonorId(donor.getId());
        var certificate = contributionService.getCertificate(saved.getId());

        assertEquals(1, history.size());
        assertEquals(saved.getId(), history.get(0).getId());
        assertEquals("Donation Acknowledgement Certificate", certificate.certificateTitle());
        assertEquals("Test Donor", certificate.donorName());
        assertEquals(125.0, certificate.amount(), 0.001);
    }

    @Test
    void rejectsMoneyContributionLargerThanRemainingGoal() {
        authenticate(ngo.getEmail(), "NGO");
        Need request = new Need();
        request.setTitle("Small Goal");
        request.setDescription("Test campaign");
        request.setType("MONEY");
        request.setTargetAmount(100.0);
        Need need = needService.saveNeed(request);

        authenticate(donor.getEmail(), "DONOR");
        Contribution contribution = new Contribution();
        contribution.setType("MONEY");
        contribution.setAmount(101.0);
        Need needReference = new Need();
        needReference.setId(need.getId());
        contribution.setNeed(needReference);

        assertThrows(ResponseStatusException.class, () -> contributionService.saveContribution(contribution));
        assertEquals(0.0, needRepository.findById(need.getId()).orElseThrow().getCurrentAmount(), 0.001);
    }

    private void authenticate(String email, String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        email,
                        "test",
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }
}
